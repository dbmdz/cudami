package de.digitalcollections.cudami.server.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.commons.springboot.actuator",
      "de.digitalcollections.commons.springboot.contributor",
      "de.digitalcollections.commons.springboot.monitoring"
    })
public class SpringConfig {

  /**
   * Create a resource bundle for your messages ("messages.properties").<br>
   * This file goes in src/main/resources because you want it to appear at the root of the classpath
   * on deployment.
   *
   * @return message source
   */
  @Bean(name = "messageSource")
  public MessageSource configureMessageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setCacheSeconds(5);
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }
}
