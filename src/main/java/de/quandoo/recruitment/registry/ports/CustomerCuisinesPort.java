package de.quandoo.recruitment.registry.ports;

import de.quandoo.recruitment.registry.adapters.inmemory.CustomerCuisinesInMemoryAdapter;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import java.util.List;

public interface CustomerCuisinesPort {

  static CustomerCuisinesPort getDefaultInstance() {
    return new CustomerCuisinesInMemoryAdapter();
  }

  void register(Customer customer, Cuisine cuisine);

  List<Cuisine> customerCuisines(Customer customer);
}
