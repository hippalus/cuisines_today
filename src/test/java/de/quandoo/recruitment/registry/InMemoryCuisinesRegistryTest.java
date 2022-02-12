package de.quandoo.recruitment.registry;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.quandoo.recruitment.registry.api.CuisinesRegistry;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import de.quandoo.recruitment.registry.ports.CustomerCuisinesPort;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCuisinesRegistryTest {

  private CuisinesRegistry cuisinesRegistry;

  @BeforeEach
  void setUp() {
    cuisinesRegistry = new CuisinesRegistryImpl(
        CuisineCustomersPort.getDefaultInstance(),
        CustomerCuisinesPort.getDefaultInstance()
    );
  }

  @AfterEach
  void tearDown() {
    cuisinesRegistry = null;
  }

  @Test
  void shouldRegisterCuisineForCustomer() {
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("italian"));

    final List<Customer> frCustomerList = cuisinesRegistry.cuisineCustomers(new Cuisine("french"));

    assertThat(frCustomerList).isEqualTo(List.of(new Customer("1")));
  }

  @Test
  void shouldRegisterCustomerForCuisine() {
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("italian"));

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
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("4"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("5"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("6"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("7"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("8"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("9"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("10"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("11"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("12"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("french"));

    //when:
    final List<Cuisine> top1Cuisines = cuisinesRegistry.topCuisines(1);
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);
    final List<Cuisine> top3Cuisines = cuisinesRegistry.topCuisines(3);

    //then:
    assertThat(top1Cuisines).isEqualTo(List.of(Cuisine.of("german")));
    assertThat(top2Cuisines).containsExactly(Cuisine.of("german"), Cuisine.of("turkey"));
    assertThat(top3Cuisines).containsExactly(Cuisine.of("german"), Cuisine.of("turkey"), Cuisine.of("italian"));
  }


}
