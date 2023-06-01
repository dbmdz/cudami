package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.model.config.IiifServerConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iiif")
@SuppressFBWarnings
public class BackendIiifServerConfig extends IiifServerConfig {}
