package de.digitalcollections.cudami.server.config;

import de.digitalcollections.commons.file.config.SpringConfigCommonsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Backend configuration.
 */
@Configuration
@Import({SpringConfigBackendDatabase.class, SpringConfigCommonsFile.class})
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

}
