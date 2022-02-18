package de.quandoo.recruitment.registry;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.random.RandomGenerator;
import javax.net.ServerSocketFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import redis.embedded.RedisServer;

@Slf4j
public abstract class AbstractIntegrationTest {

  public static final int PORT_RANGE_MIN = 1024;
  public static final int PORT_RANGE_MAX = 65535;
  public static final RandomGenerator RANDOM = RandomGenerator.getDefault();

  protected static RedisServer REDIS_SERVER;
  protected static int port;

  @SneakyThrows
  @BeforeAll
  public static void startCluster() {
    stopCluster();
    log.info("Starting Redis Server");
    port = findAvailableTcpPort();
    REDIS_SERVER = RedisServer.builder().port(port).build();
    REDIS_SERVER.start();
  }

  @AfterAll
  public static void stopCluster() {
    log.info("Stopping Redis Server");
    if (REDIS_SERVER != null) {
      REDIS_SERVER.stop();
    }
  }

  public static int findAvailableTcpPort() {
    return findAvailableTcpPort(PORT_RANGE_MIN);
  }

  public static int findAvailableTcpPort(int minPort) {
    return findAvailableTcpPort(minPort, PORT_RANGE_MAX);
  }

  public static int findAvailableTcpPort(int minPort, int maxPort) {
    return SocketType.TCP.findAvailablePort(minPort, maxPort);
  }

  private enum SocketType {

    TCP {
      @Override
      protected boolean isPortAvailable(int port) {
        try {
          ServerSocket serverSocket = ServerSocketFactory.getDefault()
              .createServerSocket(port, 1, InetAddress.getByName("localhost"));
          serverSocket.close();
          return true;
        } catch (Exception ex) {
          return false;
        }
      }
    },

    UDP {
      @Override
      protected boolean isPortAvailable(int port) {
        try {
          DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("localhost"));
          socket.close();
          return true;
        } catch (Exception ex) {
          return false;
        }
      }
    };

    protected abstract boolean isPortAvailable(int port);

    private int findRandomPort(int minPort, int maxPort) {
      int portRange = maxPort - minPort;
      return minPort + RANDOM.nextInt(portRange + 1);
    }

    int findAvailablePort(int minPort, int maxPort) {
      int portRange = maxPort - minPort;
      int candidatePort;
      int searchCounter = 0;
      do {
        if (searchCounter > portRange) {
          throw new IllegalStateException(String.format(
              "Could not find an available %s port in the range [%d, %d] after %d attempts",
              name(), minPort, maxPort, searchCounter));
        }
        candidatePort = findRandomPort(minPort, maxPort);
        searchCounter++;
      }
      while (!isPortAvailable(candidatePort));

      return candidatePort;
    }
  }
}
