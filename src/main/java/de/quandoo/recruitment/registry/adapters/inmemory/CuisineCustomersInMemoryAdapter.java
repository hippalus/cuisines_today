package de.quandoo.recruitment.registry.adapters.inmemory;

import com.google.common.base.Preconditions;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

public class CuisineCustomersInMemoryAdapter implements CuisineCustomersPort {

  private final ConcurrentMap<Cuisine, Set<Customer>> cuisineCustomers;
  private final PriorityBlockingQueue<Cuisine> topCuisinesQueue;

  public CuisineCustomersInMemoryAdapter() {
    this.cuisineCustomers = new ConcurrentHashMap<>();
    this.topCuisinesQueue = new PriorityBlockingQueue<>(11, cuisineComparator());
  }

  private Comparator<Cuisine> cuisineComparator() {
    return Comparator.comparingInt((Cuisine c) -> cuisineCustomers.get(c).size()).reversed();
  }

  @Override
  public void register(final Cuisine cuisine, final Customer customer) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    this.cuisineCustomers.putIfAbsent(cuisine, ConcurrentHashMap.newKeySet());
    this.cuisineCustomers.get(cuisine).add(customer);
    updateTopCuisines(cuisine);
  }

  private void updateTopCuisines(Cuisine cuisine) {
    this.topCuisinesQueue.removeIf(c -> c.name().equals(cuisine.name()));
    this.topCuisinesQueue.add(cuisine);
  }

  @Override
  public List<Cuisine> topCuisines(final int n) {
    Preconditions.checkArgument(n > 0, "n should be greater than zero!");
    return this.topCuisinesQueue.stream().limit(n).toList();
  }

  @Override
  public List<Customer> cuisineCustomers(final Cuisine cuisine) {
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    return Optional.ofNullable(this.cuisineCustomers.get(cuisine))
        .map(ArrayList::new)
        .orElse(new ArrayList<>());
  }
}

