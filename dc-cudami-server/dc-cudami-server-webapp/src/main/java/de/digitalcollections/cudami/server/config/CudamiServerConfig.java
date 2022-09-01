package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.TypeDeclarations;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "cudami")
@ConstructorBinding
public class CudamiServerConfig extends CudamiConfig {

  public CudamiServerConfig(
      Defaults defaults,
      UrlAlias urlAlias,
      int offsetForAlternativePaging,
      TypeDeclarations typeDeclarations) {
    super(defaults, urlAlias, offsetForAlternativePaging, typeDeclarations);
  }
}
