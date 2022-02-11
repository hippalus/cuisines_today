package de.quandoo.recruitment.registry;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.quandoo.recruitment.registry.adaters.inmemory.CuisineCustomersInMemoryAdapter;
import de.quandoo.recruitment.registry.adaters.inmemory.CustomerCuisinesInMemoryAdapter;
import de.quandoo.recruitment.registry.api.CuisinesRegistry;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCuisinesRegistryTest {

  private CuisinesRegistry cuisinesRegistry;

  @BeforeEach
  void setUp() {
    cuisinesRegistry = new CuisinesRegistryImpl(new CuisineCustomersInMemoryAdapter(), new CustomerCuisinesInMemoryAdapter());
  }

  @AfterEach
  void tearDown() {
    cuisinesRegistry = null;
  }

  @Test
  void shouldRegisterCuisineForCustomer() {
    cuisinesRegistry.register(new Customer("1"), new Cuisine("french"));
    cuisinesRegistry.register(new Customer("2"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("3"), new Cuisine("italian"));

    final List<Customer> frCustomerList = cuisinesRegistry.cuisineCustomers(new Cuisine("french"));

    assertThat(frCustomerList).isEqualTo(List.of(new Customer("1")));
  }

  @Test
  void shouldRegisterCustomerForCuisine() {
    cuisinesRegistry.register(new Customer("1"), new Cuisine("french"));
    cuisinesRegistry.register(new Customer("2"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("3"), new Cuisine("italian"));

    final List<Cuisine> frCustomerList = cuisinesRegistry.customerCuisines(new Customer("2"));

    assertThat(frCustomerList).isEqualTo(List.of(new Cuisine("german")));
  }

  @Test
  void shouldThrowExceptionNullCuisineOnGetCustomers() {
    assertThrows(NullPointerException.class, () -> cuisinesRegistry.cuisineCustomers(null), "Cuisine could not be null!");
  }

  @Test
  void shouldThrowExceptionNullCustomerOnGetCuisines() {
    assertThrows(NullPointerException.class, () -> cuisinesRegistry.customerCuisines(null), "Customer could not be null!");
  }

  @Test
  void shouldGetTopNCuisines() {
    //given:
    cuisinesRegistry.register(new Customer("1"), new Cuisine("french"));
    cuisinesRegistry.register(new Customer("2"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("3"), new Cuisine("italian"));
    cuisinesRegistry.register(new Customer("4"), new Cuisine("italian"));
    cuisinesRegistry.register(new Customer("5"), new Cuisine("italian"));
    cuisinesRegistry.register(new Customer("6"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("7"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("8"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("9"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("10"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("11"), new Cuisine("turkey"));
    cuisinesRegistry.register(new Customer("12"), new Cuisine("turkey"));
    cuisinesRegistry.register(new Customer("13"), new Cuisine("turkey"));
    cuisinesRegistry.register(new Customer("13"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("13"), new Cuisine("french"));

    //when:
    final List<Cuisine> top1Cuisines = cuisinesRegistry.topCuisines(1);
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);
    final List<Cuisine> top3Cuisines = cuisinesRegistry.topCuisines(3);

    //then:
    assertThat(top1Cuisines).isEqualTo(List.of(new Cuisine("german")));
    assertThat(top2Cuisines).containsExactly(new Cuisine("german"), new Cuisine("turkey"));
    assertThat(top3Cuisines).containsExactly(new Cuisine("german"), new Cuisine("turkey"), new Cuisine("italian"));
  }


}
