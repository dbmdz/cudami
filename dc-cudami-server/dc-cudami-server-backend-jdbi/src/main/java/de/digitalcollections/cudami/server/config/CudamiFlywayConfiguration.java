package de.digitalcollections.cudami.server.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.FlywayConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!TEST")
public class CudamiFlywayConfiguration extends FlywayConfiguration {

  @Primary
  @Bean(name = "flywayInitializer")
  @DependsOn("springUtility")
  public FlywayMigrationInitializer flywayInitializer(
      Flyway flyway, ObjectProvider<FlywayMigrationStrategy> migrationStrategy) {
    return super.flywayInitializer(flyway, migrationStrategy);
  }
}
