package de.quandoo.recruitment.registry.adaters.inmemory;

import com.google.common.base.Preconditions;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CuisineCustomersInMemoryAdapter implements CuisineCustomersPort {

  private final ConcurrentMap<Cuisine, Set<Customer>> map;

  public CuisineCustomersInMemoryAdapter() {
    this(new ConcurrentHashMap<>());
  }

  @Override
  public void register(final Cuisine cuisine, final Customer customer) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    this.map.putIfAbsent(cuisine, ConcurrentHashMap.newKeySet());
    this.map.get(cuisine).add(customer);
  }

  @Override
  public List<Cuisine> topCuisines(final int n) {
    Preconditions.checkArgument(n > 0, "n should be greater than zero!");
    return this.map.entrySet().stream().sorted(cuisineComparator()).limit(n)
        .map(Entry::getKey)
        .toList();
  }

  private Comparator<Entry<Cuisine, Set<Customer>>> cuisineComparator() {
    return Comparator.comparingInt((Entry<Cuisine, Set<Customer>> e) -> e.getValue().size()).reversed();
  }

  @Override
  public List<Customer> cuisineCustomers(final Cuisine cuisine) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    return Optional.ofNullable(map.get(cuisine))
        .map(ArrayList::new)
        .orElse(new ArrayList<>());
  }
}

