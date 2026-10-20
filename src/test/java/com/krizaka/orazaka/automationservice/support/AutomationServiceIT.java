package com.krizaka.orazaka.automationservice.support;

import com.krizaka.orazaka.test.architecture.SqlBoundaryRules;
import com.krizaka.test.container.ServiceRoles;
import java.nio.file.Path;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * A real PostgreSQL with this context's own bootstrap ({@code 50-automation.sql}, run as the
 * database's superuser exactly as {@code orazaka start} does) and the service pointed at it under
 * its own role. One container per JVM; nothing is mocked below the HTTP port.
 */
public abstract class AutomationServiceIT {

  /** The shared HS256 secret the tests sign session tokens with. */
  public static final String JWT_SECRET = "automation-it-identity-jwt-secret-256bit!!";

  private static final String ROLE = "orazaka_automation";
  private static final String PASSWORD = "orazaka_automation_pass";
  private static final String DATABASE = "orazaka_automation_db";

  @SuppressWarnings("resource")
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(
              DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
          .withDatabaseName("bootstrap")
          .withUsername("postgres")
          .withPassword("postgres")
          .withCopyFileToContainer(
              MountableFile.forHostPath(
                  SqlBoundaryRules.locateInitDb(Path.of(System.getProperty("user.dir")))
                      .resolve("50-automation.sql")),
              "/docker-entrypoint-initdb.d/50-automation.sql");

  static {
    if (System.getProperty("api.version") == null && System.getenv("DOCKER_API_VERSION") == null) {
      System.setProperty("api.version", "1.43");
    }
    POSTGRES.start();
    ServiceRoles.assignPassword(POSTGRES, ROLE, PASSWORD);
  }

  /**
   * Points the service at the container and keeps the broker out of the picture.
   *
   * @param registry the dynamic properties
   */
  @DynamicPropertySource
  static void wiring(DynamicPropertyRegistry registry) {
    registry.add(
        "orazaka.automation-service.datasource.url",
        () ->
            "jdbc:postgresql://%s:%d/%s"
                .formatted(POSTGRES.getHost(), POSTGRES.getMappedPort(5432), DATABASE));
    registry.add("orazaka.automation-service.datasource.username", () -> ROLE);
    registry.add("orazaka.automation-service.datasource.password", () -> PASSWORD);
    registry.add("krizaka.security.jwt.secret", () -> JWT_SECRET);
    // No broker, as in CI: the listeners do not start and a connection attempt fails fast and
    // quietly.
    registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
    registry.add("spring.rabbitmq.host", () -> "127.0.0.1");
    registry.add("spring.rabbitmq.port", () -> "1");
    registry.add("management.tracing.sampling.probability", () -> "0.0");
  }
}
