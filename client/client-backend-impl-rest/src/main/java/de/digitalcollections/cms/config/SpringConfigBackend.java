package de.digitalcollections.cms.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.cms.client.backend.impl.repository"
})
//@PropertySource(value = {
//  "classpath:de/digitalcollections/cms/config/SpringConfigBackend-${spring.profiles.active:local}.properties"
//})
public class SpringConfigBackend {
  
}
