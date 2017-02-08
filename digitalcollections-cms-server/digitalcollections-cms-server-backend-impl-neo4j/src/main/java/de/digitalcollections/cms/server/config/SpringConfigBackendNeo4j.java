package de.digitalcollections.cms.server.config;

import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories(basePackages = "de.digitalcollections.cms.server.backend.impl.neo4j.repository")
@ComponentScan(basePackages = "de.digitalcollections.cms.server.backend.impl.neo4j")
@EnableTransactionManagement
@PropertySource(
        "classpath:de/digitalcollections/cms/server/config/SpringConfigBackendNeo4j-${spring.profiles.active}.properties")
public class SpringConfigBackendNeo4j {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendNeo4j.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Value("${database.hostname}")
  private String hostname;

  @Value("${database.password}")
  private String password;

  @Value("${database.port}")
  private Long port;

  @Value("${database.username}")
  private String username;

  @Bean
  public SessionFactory sessionFactory() {
    return new SessionFactory(getConfiguration(), "de.digitalcollections.cms.server.backend.impl.neo4j.model", "de.digitalcollections.cms.model.api");
  }

  @Bean
  public Neo4jTransactionManager transactionManager() {
    return new Neo4jTransactionManager(sessionFactory());
  }

  protected org.neo4j.ogm.config.Configuration getConfiguration() {
    org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
    if (port == 7474) {
      // HTTP
      final String url = String.format("http://%s:%d", hostname, port);
      config.driverConfiguration()
              .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
              .setCredentials(username, password)
              .setURI(url);
    } else {
      // Bolt
      final String url = String.format("bolt://%s:%s@%s:%d", username, password, hostname, port);
      config.driverConfiguration()
              .setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver")
              .setURI(url);
    }
    return config;
  }
}
