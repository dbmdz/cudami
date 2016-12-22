package de.digitalcollections.cms.server.config;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.neo4j.ogm.config.DriverConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfigBackendNeo4jForTest extends SpringConfigBackendNeo4j {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendNeo4jForTest.class);

  private static final String RAMDISK = "/mnt/ramdisk";

  @Override
  protected org.neo4j.ogm.config.Configuration getConfiguration() {
    org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
    DriverConfiguration driverConfiguration = config.driverConfiguration()
            .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
    if (Files.exists(Paths.get(RAMDISK))) {
      LOGGER.info("Using RAM disk at " + RAMDISK + " for embedded database.");
      driverConfiguration.setURI("file://" + RAMDISK);
    }
    return config;
  }
}
