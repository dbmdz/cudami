package de.digitalcollections.cudami.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.file.config.SpringConfigCommonsFile;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Backend configuration.
 */
@Configuration
@Import({SpringConfigBackendDatabase.class, SpringConfigCommonsFile.class})
public class SpringConfigBackend implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    // customize default spring boot jackson objectmapper
    DigitalCollectionsObjectMapper.customize(objectMapper);
  }
}
