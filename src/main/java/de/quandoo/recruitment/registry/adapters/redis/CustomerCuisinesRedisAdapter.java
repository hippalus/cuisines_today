package de.quandoo.recruitment.registry.adapters.redis;

import com.google.common.base.Preconditions;
import de.quandoo.recruitment.registry.adapters.redis.entitiy.CuisineREntity;
import de.quandoo.recruitment.registry.adapters.redis.entitiy.CustomerREntity;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CustomerCuisinesPort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;

public class CustomerCuisinesRedisAdapter implements CustomerCuisinesPort {

  private final RMapCache<CustomerREntity, RSetCache<CuisineREntity>> cache;
  private final RedissonClient redissonClient;

  public CustomerCuisinesRedisAdapter(final RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
    this.cache = redissonClient.getMapCache("customer-cuisines-cache");
  }


  @Override
  public void register(final Customer customer, final Cuisine cuisine) {
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    this.cache.computeIfAbsent(
        CustomerREntity.of(customer),
        c -> redissonClient.getSetCache(getCuisineSetCacheByCustomer(customer))
    ).add(CuisineREntity.of(cuisine));
  }

  private String getCuisineSetCacheByCustomer(Customer customer) {
    return customer.uuid() + "cuisines-set-cache";
  }

  @Override
  public List<Cuisine> customerCuisines(final Customer customer) {
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    return Optional.ofNullable(this.cache.get(CustomerREntity.of(customer)))
        .map(RSetCache::readAll)
        .stream()
        .filter(Objects::nonNull)
        .mapMulti(this::toModel)
        .toList();
  }

  // using mapMulti (jdk17) instead of flatmap for stream performance
  private void toModel(Set<CuisineREntity> cuisineREntities, Consumer<Cuisine> consumer) {
    cuisineREntities.forEach(cuisineREntity -> consumer.accept(cuisineREntity.toModel()));
  }
}
