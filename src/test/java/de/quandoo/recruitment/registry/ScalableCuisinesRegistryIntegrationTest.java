package de.quandoo.recruitment.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.quandoo.recruitment.registry.adapters.redis.CuisineCustomersRedisAdapter;
import de.quandoo.recruitment.registry.adapters.redis.CustomerCuisinesRedisAdapter;
import de.quandoo.recruitment.registry.api.CuisinesRegistry;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class ScalableCuisinesRegistryIntegrationTest extends AbstractIntegrationTest {

  private static CuisinesRegistry cuisinesRegistry;

  @BeforeAll
  public static void beforeAll() {
    AbstractIntegrationTest.startCluster();
    final Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");
    config.setCodec(JsonJacksonCodec.INSTANCE);
    final RedissonClient redissonClient = Redisson.create(config);
    cuisinesRegistry = new CuisinesRegistryImpl(new CuisineCustomersRedisAdapter(redissonClient),
        new CustomerCuisinesRedisAdapter(redissonClient));
    fillTestData();
  }

  private static void fillTestData() {
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
    cuisinesRegistry.register(Customer.of("14"), Cuisine.of("turkey"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("french"));

  }

  @AfterAll
  public static void afterAll() {
    AbstractIntegrationTest.stopCluster();
    cuisinesRegistry = null;
  }

  @Test
  void shouldRegisterCuisineForCustomer() {
    final List<Customer> frCustomerList = cuisinesRegistry.cuisineCustomers(new Cuisine("french"));
    assertEquals(List.of(new Customer("1"), new Customer("13")), frCustomerList);
  }

  @Test
  void shouldRegisterCustomerForCuisine() {
    final List<Cuisine> frCustomerList = cuisinesRegistry.customerCuisines(new Customer("2"));
    final List<Cuisine> multiCuisineCustomerList = cuisinesRegistry.customerCuisines(new Customer("13"));

    assertThat(frCustomerList).isEqualTo(List.of(Cuisine.of("german")));
    assertThat(multiCuisineCustomerList)
        .containsExactlyInAnyOrder(Cuisine.of("french"), Cuisine.of("turkey"), Cuisine.of("german"));
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
    //when:
    final List<Cuisine> top1Cuisines = cuisinesRegistry.topCuisines(1);
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);
    final List<Cuisine> top3Cuisines = cuisinesRegistry.topCuisines(3);

    //then:
    assertThat(top1Cuisines).containsExactly(Cuisine.of("german"));
    assertThat(top2Cuisines).containsExactly(Cuisine.of("german"), Cuisine.of("turkey"));
    assertThat(top3Cuisines).containsExactly(Cuisine.of("german"), Cuisine.of("turkey"), Cuisine.of("italian"));
  }

}
