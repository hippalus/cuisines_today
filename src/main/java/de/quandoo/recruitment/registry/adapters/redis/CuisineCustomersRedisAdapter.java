package de.quandoo.recruitment.registry.adapters.redis;

import com.google.common.base.Preconditions;
import de.quandoo.recruitment.registry.adapters.redis.entitiy.CuisineREntity;
import de.quandoo.recruitment.registry.adapters.redis.entitiy.CustomerREntity;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;

public class CuisineCustomersRedisAdapter implements CuisineCustomersPort {

  private final RMapCache<CuisineREntity, RSetCache<CustomerREntity>> cuisineCustomersCache;
  private final RedissonClient redissonClient;

  public CuisineCustomersRedisAdapter(final RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
    this.cuisineCustomersCache = redissonClient.getMapCache("cuisine-customers-cache");
  }

  @Override
  public void register(final Cuisine cuisine, final Customer customer) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    final CuisineREntity cuisineREntity = CuisineREntity.of(cuisine);
    final CustomerREntity customerREntity = CustomerREntity.of(customer);
    this.cuisineCustomersCache.computeIfAbsent(cuisineREntity, createNewCustomerSetCacheForCuisine()).add(customerREntity);
  }

  private Function<CuisineREntity, RSetCache<CustomerREntity>> createNewCustomerSetCacheForCuisine() {
    return cuisineR -> redissonClient.getSetCache(cuisineR.getName() + "-customers-set-cache");
  }

  //TODO: optimize this method. Use Max Heap Tree data structure with redis.
  // each topCuisines method invokes should be max O(n) but it time complexity is O(nlogn)
  @Override
  public List<Cuisine> topCuisines(final int n) {
    Preconditions.checkArgument(n > 0, "n should be greater than zero!");
    return this.cuisineCustomersCache.entrySet()
        .stream()
        .sorted(cuisineComparator())
        .limit(n)
        .map(Entry::getKey)
        .map(CuisineREntity::toModel)
        .toList();
  }

  private Comparator<Entry<CuisineREntity, RSetCache<CustomerREntity>>> cuisineComparator() {
    return Comparator.comparingInt((Entry<CuisineREntity, RSetCache<CustomerREntity>> o) -> o.getValue().size()).reversed();
  }

  @Override
  public List<Customer> cuisineCustomers(final Cuisine cuisine) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    return Optional.ofNullable(cuisineCustomersCache.get(CuisineREntity.of(cuisine)))
        .stream().filter(Objects::nonNull)
        .mapMulti(this::toModel)
        .toList();
  }

  // using mapMulti (jdk17) instead of flatmap for stream performance
  private void toModel(RSetCache<CustomerREntity> customerREntities, Consumer<Customer> consumer) {
    customerREntities.forEach(customerREntity -> consumer.accept(customerREntity.toModel()));
  }
}
