package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Backend configuration.
 */
@Configuration
@Import({SpringConfigBackendDatabase.class})
@ComponentScan(basePackages = {
  "de.digitalcollections.cudami.server.backend.impl.jdbi"
})
public class SpringConfigBackend implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    CudamiObjectMapper.customize(objectMapper);
  }
}
