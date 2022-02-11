package de.quandoo.recruitment.registry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import redis.embedded.RedisServer;

@Slf4j
public abstract class AbstractIntegrationTest {

  protected static RedisServer REDIS_SERVER;

  @SneakyThrows
  @BeforeAll
  public static void startCluster() {
    stopCluster();
    log.info("Starting Redis Server");
    REDIS_SERVER = RedisServer.builder().port(6379).build();
    REDIS_SERVER.start();
  }

  @AfterAll
  public static void stopCluster() {
    log.info("Stopping Redis Server");
    if (REDIS_SERVER != null && REDIS_SERVER.isActive()) {
      REDIS_SERVER.stop();
    }
  }
}
