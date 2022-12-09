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
      int offsetForAlternativePaging,
      String repositoryFolderPath,
      TypeDeclarations typeDeclarations,
      UrlAlias urlAlias) {
    super(defaults, offsetForAlternativePaging, repositoryFolderPath, typeDeclarations, urlAlias);
  }
}
