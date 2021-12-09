package de.digitalcollections.cudami.client.config;

import de.digitalcollections.model.identifiable.entity.EntityType;
import java.util.List;
import java.util.Locale;

public class CudamiConfig {

  UrlAlias urlAlias;
  Defaults defaults;

  public UrlAlias getUrlAlias() {
    return urlAlias;
  }

  public void setUrlAlias(UrlAlias urlAlias) {
    this.urlAlias = urlAlias;
  }

  public Defaults getDefaults() {
    return defaults;
  }

  public void setDefaults(Defaults defaults) {
    this.defaults = defaults;
  }

  public static class UrlAlias {

    private List<EntityType> generationExcludes;
    private int maxLength = -1;

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

  public static class Defaults {

    String language;
    Locale locale;

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }
  }
}
