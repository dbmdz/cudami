package de.digitalcollections.cudami.server.config;

import de.digitalcollections.model.identifiable.entity.EntityType;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cudami.urlalias")
public class UrlAliasGenerationProperties {

  private List<EntityType> generationExcludes;

  public UrlAliasGenerationProperties() {}

  public List<EntityType> getGenerationExcludes() {
    return this.generationExcludes;
  }

  public void setGenerationExcludes(List<EntityType> generationExcludes) {
    this.generationExcludes = generationExcludes;
  }
}
