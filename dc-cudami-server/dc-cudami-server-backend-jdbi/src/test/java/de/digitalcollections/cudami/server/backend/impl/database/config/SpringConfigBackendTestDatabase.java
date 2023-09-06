package de.digitalcollections.cudami.server.backend.impl.database.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.CudamiConfig.Defaults;
import de.digitalcollections.cudami.model.config.CudamiConfig.UrlAlias;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Identifier;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Image;
import de.digitalcollections.cudami.model.config.IiifServerConfig.Presentation;
import de.digitalcollections.cudami.server.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.ArrayList;
import java.util.Locale;
import javax.sql.DataSource;
import org.jdbi.v3.testing.junit5.JdbiExtension;
import org.jdbi.v3.testing.junit5.tc.JdbiTestcontainersExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@ComponentScan(
    basePackages = {"de.digitalcollections.cudami.server.backend.impl.jdbi"},
    basePackageClasses = SpringConfigBackendDatabase.class)
public class SpringConfigBackendTestDatabase {

  private static HikariPool CONNECTION_POOL;
  private static CudamiConfig CUDAMI_CONFIG =
      new CudamiConfig(
          new Defaults("en", Locale.forLanguageTag("en-US")),
          5000,
          "/tmp/cudami/fileResources",
          null,
          new UrlAlias(new ArrayList<>(), 64));

  private static PostgreSQLContainer postgreSQLContainer;

  @RegisterExtension
  JdbiExtension extension = JdbiTestcontainersExtension.instance(postgreSQLContainer);

  static {
    postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:12"));
    postgreSQLContainer.start();
  }

  @Bean
  @Primary
  public ObjectMapper testObjectMapper() {
    return new DigitalCollectionsObjectMapper();
  }

  @Bean
  @Primary
  public DataSource testDataSource() {
    if (CONNECTION_POOL == null) {
      assert postgreSQLContainer.isRunning();
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
      config.setUsername("test");
      config.setPassword("test");
      config.setDriverClassName(postgreSQLContainer.getDriverClassName());
      config.setMaximumPoolSize(100);
      config.setMinimumIdle(10);
      CONNECTION_POOL = new HikariPool(config);
    }
    return CONNECTION_POOL.getUnwrappedDataSource();
  }

  @Bean
  @Primary
  CudamiConfig testCudamiConfig() {
    return CUDAMI_CONFIG;
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
