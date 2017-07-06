package de.digitalcollections.cudami.server.config;

import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories(basePackages = "de.digitalcollections.cudami.server.backend.impl.neo4j.repository")
@ComponentScan(basePackages = "de.digitalcollections.cudami.server.backend.impl.neo4j")
@EnableTransactionManagement
@PropertySource(
        "classpath:de/digitalcollections/cudami/server/config/SpringConfigBackendNeo4j-${spring.profiles.active}.properties")
public class SpringConfigBackendNeo4jForTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendNeo4jForTest.class);

  @Bean
  public org.neo4j.ogm.config.Configuration getConfiguration() {
    org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
    config
            .driverConfiguration()
            .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
    return config;
  }

  @Bean
  public SessionFactory sessionFactory() {
    SessionFactory sessionFactory = new SessionFactory(getConfiguration(), "de.digitalcollections.cudami.server.backend.impl.neo4j.model", "de.digitalcollections.cudami.model.api");
    return sessionFactory;
  }

  @Bean
  public Neo4jTransactionManager transactionManager() {
    return new Neo4jTransactionManager(sessionFactory());
  }

}
