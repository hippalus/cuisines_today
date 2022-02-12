package de.quandoo.recruitment.registry.ports;

import de.quandoo.recruitment.registry.adapters.inmemory.CuisineCustomersInMemoryAdapter;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import java.util.List;

public interface CuisineCustomersPort {

  static CuisineCustomersPort getDefaultInstance() {
    return new CuisineCustomersInMemoryAdapter();
  }

  void register(Cuisine cuisine, Customer customer);

  List<Cuisine> topCuisines(int n);

  List<Customer> cuisineCustomers(Cuisine cuisine);

}
