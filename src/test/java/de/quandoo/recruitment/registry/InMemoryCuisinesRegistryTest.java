package de.quandoo.recruitment.registry;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
  void shouldRegisterSameCuisineForCustomer() {
    //given:
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("french"));

    //when:
    final List<Customer> frCustomerList = cuisinesRegistry.cuisineCustomers(new Cuisine("french"));

    //then:
    assertThat(frCustomerList).containsExactlyInAnyOrder(Customer.of("1"), Customer.of("2"), Customer.of("3"));
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
  void shouldRegisterSameCustomerForCuisine() {
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("italian"));

    final List<Cuisine> frCustomerList = cuisinesRegistry.customerCuisines(new Customer("1"));

    assertThat(frCustomerList).containsExactlyInAnyOrder(Cuisine.of("german"), Cuisine.of("french"), Cuisine.of("italian"));
  }

  @Test
  void shouldThrowExceptionNullCuisineOnGetCustomers() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> cuisinesRegistry.cuisineCustomers(null))
        .withMessage("Cuisine could not be null!");
  }

  @Test
  void shouldThrowExceptionNullCustomerOnGetCuisines() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> cuisinesRegistry.customerCuisines(null))
        .withMessage("Customer could not be null!");
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

  @Test
  void shouldGetTopNCuisinesSameOrder() {
    //given:
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("french"));
    //six
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("6"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("7"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("8"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("9"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("10"), Cuisine.of("german"));
    //six
    cuisinesRegistry.register(Customer.of("11"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("12"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("14"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("15"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("16"), Cuisine.of("turkey"));

    //when:
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);

    //then:
    assertThat(top2Cuisines).containsExactly(Cuisine.of("german"), Cuisine.of("turkey"));
  }

  @Test
  void shouldThrowIllegalArgExWhenTopNCuisinesLessThan1() {

    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("4"), Cuisine.of("italian"));

    assertThrows(IllegalArgumentException.class, () -> cuisinesRegistry.topCuisines(0), "n should be greater than zero!");
    assertThrows(IllegalArgumentException.class, () -> cuisinesRegistry.topCuisines(-5), "n should be greater than zero!");
  }

  @Test
  void shouldThrowNullPointerExWhenRegisterNullArg() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> cuisinesRegistry.register(null, null));

    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> cuisinesRegistry.register(null, Cuisine.of("french")))
        .withMessage("Customer could not be null!");

    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> cuisinesRegistry.register(Customer.of("2"), null))
        .withMessage("Cuisine could not be null!");
  }


}
