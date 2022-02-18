package de.quandoo.recruitment.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import de.quandoo.recruitment.registry.adapters.redis.CuisineCustomersRedisAdapter;
import de.quandoo.recruitment.registry.adapters.redis.CustomerCuisinesRedisAdapter;
import de.quandoo.recruitment.registry.api.CuisinesRegistry;
import de.quandoo.recruitment.registry.model.Cuisine;
import de.quandoo.recruitment.registry.model.Customer;
import de.quandoo.recruitment.registry.ports.CuisineCustomersPort;
import de.quandoo.recruitment.registry.ports.CustomerCuisinesPort;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class CuisinesRegistryRedisIntegrationTest extends AbstractIntegrationTest {

  private static CuisinesRegistry cuisinesRegistry;

  @BeforeAll
  public static void beforeAll() {
    AbstractIntegrationTest.startCluster();

    final Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:" + port);
    config.setCodec(JsonJacksonCodec.INSTANCE);
    final RedissonClient redissonClient = Redisson.create(config);

    final CuisineCustomersPort cuisineCustomersPort = new CuisineCustomersRedisAdapter(redissonClient);
    final CustomerCuisinesPort customerCuisinesPort = new CustomerCuisinesRedisAdapter(redissonClient);

    cuisinesRegistry = new CuisinesRegistryImpl(cuisineCustomersPort, customerCuisinesPort);

    fillTestData();
  }

  @AfterAll
  public static void afterAll() {
    AbstractIntegrationTest.stopCluster();
    cuisinesRegistry = null;
  }

  private static void fillTestData() {
    //given:
    cuisinesRegistry.register(Customer.of("1"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("100"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("130"), Cuisine.of("french"));
    //10
    cuisinesRegistry.register(Customer.of("2"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("6"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("7"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("8"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("9"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("10"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("20"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("60"), Cuisine.of("german"));
    cuisinesRegistry.register(Customer.of("70"), Cuisine.of("german"));
    //10
    cuisinesRegistry.register(Customer.of("11"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("12"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("13"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("14"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("110"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("120"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("130"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("140"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("150"), Cuisine.of("turkish"));
    cuisinesRegistry.register(Customer.of("160"), Cuisine.of("turkish"));

    cuisinesRegistry.register(Customer.of("3"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("4"), Cuisine.of("italian"));
    cuisinesRegistry.register(Customer.of("5"), Cuisine.of("italian"));
  }

  @Test
  void shouldRegisterCuisineForCustomer() {
    final List<Customer> frCustomerList = cuisinesRegistry.cuisineCustomers(new Cuisine("french"));
    assertThat(frCustomerList).containsExactlyInAnyOrder(
        Customer.of("1"),
        Customer.of("13"),
        Customer.of("100"),
        Customer.of("130"));
  }

  @Test
  void shouldRegisterCustomerForCuisine() {
    final List<Cuisine> frCustomerList = cuisinesRegistry.customerCuisines(new Customer("2"));
    final List<Cuisine> multiCuisineCustomerList = cuisinesRegistry.customerCuisines(new Customer("13"));

    assertThat(frCustomerList).isEqualTo(List.of(Cuisine.of("german")));
    assertThat(multiCuisineCustomerList)
        .containsExactlyInAnyOrder(Cuisine.of("french"), Cuisine.of("turkish"), Cuisine.of("german"));
  }

  @Test
  void shouldRegisterSameCustomerForCuisine() {
    cuisinesRegistry.register(Customer.of("9999999"), Cuisine.of("french"));
    cuisinesRegistry.register(Customer.of("9999999"), Cuisine.of("american"));
    cuisinesRegistry.register(Customer.of("9999999"), Cuisine.of("italian"));

    final List<Cuisine> frCustomerList = cuisinesRegistry.customerCuisines(new Customer("9999999"));

    assertThat(frCustomerList).containsExactlyInAnyOrder(Cuisine.of("american"), Cuisine.of("french"), Cuisine.of("italian"));
  }

  @Test
  void shouldThrowExceptionNullCuisineOnGetCustomers() {
    assertThatNullPointerException()
        .isThrownBy(() -> cuisinesRegistry.cuisineCustomers(null))
        .withMessage("Cuisine could not be null!");
  }

  @Test
  void shouldThrowExceptionNullCustomerOnGetCuisines() {
    assertThatNullPointerException()
        .isThrownBy(() -> cuisinesRegistry.customerCuisines(null))
        .withMessage("Customer could not be null!");
  }

  @Test
  void shouldGetTopNCuisines() {
    //when:
    final List<Cuisine> top1Cuisines = cuisinesRegistry.topCuisines(1);
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);
    final List<Cuisine> top3Cuisines = cuisinesRegistry.topCuisines(3);

    //then:
    //same priority
    final boolean turkish = top1Cuisines.contains(Cuisine.of("turkish"));
    final boolean german = top1Cuisines.contains(Cuisine.of("german"));
    assertThat(turkish || german).isTrue();

    assertThat(top2Cuisines).containsExactlyInAnyOrder(Cuisine.of("turkish"), Cuisine.of("german"));

    assertThat(top3Cuisines.get(2)).isEqualTo(Cuisine.of("french"));
  }

  @Test
  void shouldGetTopNCuisinesSameOrder() {
    //when:
    final List<Cuisine> top2Cuisines = cuisinesRegistry.topCuisines(2);

    //then:
    assertThat(top2Cuisines).containsExactlyInAnyOrder(Cuisine.of("german"), Cuisine.of("turkish"));
  }

  @Test
  void shouldThrowIllegalArgExWhenTopNCuisinesLessThan1() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> cuisinesRegistry.topCuisines(0))
        .withMessage("n should be greater than zero!");

    assertThatIllegalArgumentException()
        .isThrownBy(() -> cuisinesRegistry.topCuisines(-5))
        .withMessage("n should be greater than zero!");
  }


  @Test
  void shouldThrowNullPointerExWhenRegisterNullArg() {
    assertThatNullPointerException()
        .isThrownBy(() -> cuisinesRegistry.register(null, null));

    assertThatNullPointerException()
        .isThrownBy(() -> cuisinesRegistry.register(null, Cuisine.of("french")))
        .withMessage("Customer could not be null!");

    assertThatNullPointerException()
        .isThrownBy(() -> cuisinesRegistry.register(Customer.of("2"), null))
        .withMessage("Cuisine could not be null!");
  }

}
