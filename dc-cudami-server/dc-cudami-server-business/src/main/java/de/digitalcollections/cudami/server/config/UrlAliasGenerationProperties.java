package de.digitalcollections.cudami.server.config;

import de.digitalcollections.model.identifiable.entity.EntityType;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cudami.urlalias")
public class UrlAliasGenerationProperties {

  private List<EntityType> generationExcludes;
  private int maxLength = -1; // default, when unset

  public UrlAliasGenerationProperties() {}

  public List<EntityType> getGenerationExcludes() {
    return this.generationExcludes;
  }

  public void setGenerationExcludes(List<EntityType> generationExcludes) {
    this.generationExcludes = generationExcludes;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }
}
