package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
  "de.digitalcollections.cudami.server.backend.impl.jdbi"
})
@PropertySource(value = {
  "classpath:de/digitalcollections/cudami/config/SpringConfigBackend-${spring.profiles.active:local}.properties"
})
public class SpringConfigBackend implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Autowired
  ObjectMapper objectMapper;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    CudamiObjectMapper.customize(objectMapper);
  }
}
