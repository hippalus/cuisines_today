package de.quandoo.recruitment.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.quandoo.recruitment.registry.adaters.redis.CuisineCustomersRedisAdapter;
import de.quandoo.recruitment.registry.adaters.redis.CustomerCuisinesRedisAdapter;
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
    cuisinesRegistry.register(new Customer("14"), new Cuisine("turkey"));
    cuisinesRegistry.register(new Customer("13"), new Cuisine("german"));
    cuisinesRegistry.register(new Customer("13"), new Cuisine("french"));

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
    assertThat(frCustomerList).isEqualTo(List.of(new Cuisine("german")));
    assertThat(multiCuisineCustomerList).containsExactlyInAnyOrder(new Cuisine("french"), new Cuisine("turkey"),
        new Cuisine("german"));
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
    assertThat(top1Cuisines).containsExactly(new Cuisine("german"));
    assertThat(top2Cuisines).containsExactly(new Cuisine("german"), new Cuisine("turkey"));
    assertThat(top3Cuisines).containsExactly(new Cuisine("german"), new Cuisine("turkey"), new Cuisine("italian"));
  }

}
