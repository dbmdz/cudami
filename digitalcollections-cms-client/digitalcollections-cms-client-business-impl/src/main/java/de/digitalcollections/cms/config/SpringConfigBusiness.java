package de.digitalcollections.cms.config;

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
  "de.digitalcollections.cms.client.business.impl.service",
  "de.digitalcollections.cms.client.business.impl.validator"})
@PropertySource(value = {
  "classpath:de/digitalcollections/cms/config/SpringConfigBusiness-${spring.profiles.active:local}.properties"})
public class SpringConfigBusiness {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
