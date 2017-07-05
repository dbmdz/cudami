package de.digitalcollections.cms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Backend configuration.
 */
@Configuration
@Import({SpringConfigBackendDatabase.class})
@ComponentScan(basePackages = {
  "de.digitalcollections.cms.server.backend.impl.jpa.repository"
})
@PropertySource(value = {
  "classpath:de/digitalcollections/cms/config/SpringConfigBackend-${spring.profiles.active:local}.properties"
})
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
