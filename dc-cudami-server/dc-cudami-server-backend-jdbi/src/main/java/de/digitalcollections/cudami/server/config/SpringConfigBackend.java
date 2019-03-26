package de.digitalcollections.cudami.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Backend configuration.
 */
@Configuration
@Import({SpringConfigBackendDatabase.class})
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

}
