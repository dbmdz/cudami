package de.digitalcollections.cudami.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Services context.
 */
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.admin.business.impl.service",
  "de.digitalcollections.cudami.admin.business.impl.validator"})
@PropertySource(value = {
  "classpath:de/digitalcollections/cudami/config/SpringConfigBusiness-${spring.profiles.active:local}.properties"})
public class SpringConfigBusiness {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
