package de.quandoo.recruitment.registry.adapters.inmemory;

import com.google.common.base.Preconditions;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CustomerCuisinesPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerCuisinesInMemoryAdapter implements CustomerCuisinesPort {

  private final ConcurrentMap<Customer, Set<Cuisine>> map;

  public CustomerCuisinesInMemoryAdapter() {
    this(new ConcurrentHashMap<>());
  }

  @Override
  public void register(final Customer customer, final Cuisine cuisine) {
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    Preconditions.checkNotNull(cuisine, "Cuisine could not be null!");
    this.map.putIfAbsent(customer, ConcurrentHashMap.newKeySet());
    this.map.get(customer).add(cuisine);
  }

  @Override
  public List<Cuisine> customerCuisines(final Customer customer) {
    Preconditions.checkNotNull(customer, "Customer could not be null!");
    return Optional.ofNullable(this.map.get(customer))
        .map(ArrayList::new)
        .orElse(new ArrayList<>());
  }
}
