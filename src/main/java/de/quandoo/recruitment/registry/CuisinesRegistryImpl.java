package de.quandoo.recruitment.registry;

import de.quandoo.recruitment.registry.api.CuisinesRegistry;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import de.quandoo.recruitment.registry.ports.CustomerCuisinesPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CuisinesRegistryImpl implements CuisinesRegistry {

  private final CuisineCustomersPort cuisineCustomersPort;
  private final CustomerCuisinesPort customerCuisinesPort;

  @Override
  public void register(final Customer userId, final Cuisine cuisine) {
    customerCuisinesPort.register(userId, cuisine);
    cuisineCustomersPort.register(cuisine, userId);
  }

  @Override
  public List<Customer> cuisineCustomers(final Cuisine cuisine) {
    return cuisineCustomersPort.cuisineCustomers(cuisine);
  }

  @Override
  public List<Cuisine> customerCuisines(final Customer customer) {
    return customerCuisinesPort.customerCuisines(customer);
  }

  @Override
  public List<Cuisine> topCuisines(final int n) {
    return cuisineCustomersPort.topCuisines(n);
  }
}
