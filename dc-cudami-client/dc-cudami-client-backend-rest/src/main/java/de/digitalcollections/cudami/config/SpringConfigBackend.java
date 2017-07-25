package de.digitalcollections.cudami.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.client.backend.impl.repository"
})
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cudami/config/SpringConfigBackend-${spring.profiles.active:local}.properties"
//})
public class SpringConfigBackend {
  
}
