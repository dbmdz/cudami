package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "cudami")
@ConstructorBinding
public class CudamiServerConfig extends CudamiConfig {

  public CudamiServerConfig(Defaults defaults, UrlAlias urlAlias, int offsetForAlternativePaging) {
    super(defaults, urlAlias, offsetForAlternativePaging);
  }
}
