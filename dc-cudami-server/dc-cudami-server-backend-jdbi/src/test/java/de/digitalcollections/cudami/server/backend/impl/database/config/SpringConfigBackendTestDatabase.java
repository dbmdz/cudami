package de.digitalcollections.cudami.server.backend.impl.database.config;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.DcCommonsJdbiPlugin;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Identifier;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Image;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Presentation;
import de.digitalcollections.cudami.server.backend.impl.jdbi.plugins.JsonbJdbiPlugin;
import de.digitalcollections.cudami.server.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.junit5.JdbiExtension;
import org.jdbi.v3.testing.junit5.tc.JdbiTestcontainersExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Configuration
@ComponentScan(
    basePackages = {"de.digitalcollections.cudami.server.backend.impl.jdbi"},
    basePackageClasses = SpringConfigBackendDatabase.class)
@TestPropertySource(locations = "classpath:application.yml")
public class SpringConfigBackendTestDatabase {

  private static final DockerImageName DOCKER_IMAGE_NAME = DockerImageName.parse("postgres:12");
  private static Boolean isMigrated = false;
  private static DriverManagerDataSource DATA_SOURCE;

  @Container
  public static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer(DOCKER_IMAGE_NAME);

  @RegisterExtension
  JdbiExtension extension = JdbiTestcontainersExtension.instance(postgreSQLContainer);

  static {
    postgreSQLContainer.start();
  }

  @Bean
  @Primary
  public PostgreSQLContainer postgreSQLContainer() {
    return postgreSQLContainer;
  }

  @Bean
  @Primary
  public ObjectMapper testObjectMapper() {
    return new DigitalCollectionsObjectMapper();
  }

  //  @Bean
  //  @Primary
  //  public DataSource dataSource() {
  //    return DataSourceBuilder.create().build();
  //  }

  //  @Bean
  //  @Primary
  //  public DataSource testDataSource() {
  //    if (DATA_SOURCE == null) {
  //      DriverManagerDataSource dataSource = new DriverManagerDataSource();
  //      dataSource.setDriverClassName(postgreSQLContainer.getDriverClassName());
  //      // jdbc:postgresql://localhost:32769/test?loggerLevel=OFF
  //      dataSource.setUrl(postgreSQLContainer.getJdbcUrl());
  //      dataSource.setUsername("test");
  //      dataSource.setPassword("test");
  //      SpringConfigBackendTestDatabase.DATA_SOURCE = dataSource;
  //    }
  //    return SpringConfigBackendTestDatabase.DATA_SOURCE;
  //  }

  @Bean
  @Primary
  Jdbi testJdbi(ObjectMapper objectMapper, DataSource dataSource) {
    Jdbi jdbi = Jdbi.create(dataSource);
    jdbi.installPlugin(new SqlObjectPlugin());
    jdbi.installPlugin(new DcCommonsJdbiPlugin());
    jdbi.installPlugin(new PostgresPlugin());
    jdbi.installPlugin(new JsonbJdbiPlugin(objectMapper));

    if (!isMigrated) {
      synchronized (isMigrated) {
        Map<String, String> placeholders = Map.of("iiifBaseUrl", "foo");
        Flyway flyway =
            Flyway.configure()
                .dataSource(dataSource)
                .placeholders(placeholders)
                .locations(
                    "classpath:/de/digitalcollections/cudami/server/backend/impl/database/migration")
                .load();
        flyway.migrate();
        isMigrated = true;
      }
    }

    return jdbi;
  }

  @Bean
  @Primary
  CudamiConfig testCudamiConfig() {
    CudamiConfig cudamiConfig = Mockito.mock(CudamiConfig.class);
    when(cudamiConfig.getOffsetForAlternativePaging()).thenReturn(5000);
    return cudamiConfig;
  }

  @Bean
  @Primary
  Identifier testIiifServerConfigIdentifier() {
    return Mockito.mock(Identifier.class);
  }

  @Bean
  @Primary
  Image testIiifServerConfigImage() {
    return Mockito.mock(Image.class);
  }

  @Bean
  @Primary
  Presentation testIiifServerConfigPresentation() {
    return Mockito.mock(Presentation.class);
  }
}
