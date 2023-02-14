package de.digitalcollections.cudami.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

public class CudamiConfig {

  private Defaults defaults;
  private int offsetForAlternativePaging = 0;
  private String repositoryFolderPath;
  private TypeDeclarations typeDeclarations;
  private UrlAlias urlAlias;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "typeDeclarations are partially filled at runtime")
  @JsonCreator(mode = Mode.PROPERTIES)
  public CudamiConfig(
      @JsonProperty(value = "defaults") Defaults defaults,
      @JsonProperty(value = "offsetForAlternativePaging") int offsetForAlternativePaging,
      @JsonProperty(value = "repositoryFolderPath") String repositoryFolderPath,
      @JsonProperty(value = "typeDeclarations") TypeDeclarations typeDeclarations,
      @JsonProperty(value = "urlAlias") UrlAlias urlAlias) {
    if (defaults == null) {
      throw new IllegalStateException("Required `cudami.defaults` configuration missing.");
    }
    this.defaults = defaults;
    this.offsetForAlternativePaging = offsetForAlternativePaging;
    if (!StringUtils.hasText(repositoryFolderPath)) {
      throw new IllegalStateException(
          "Required `cudami.repositoryFolderPath` configuration missing.");
    }
    this.repositoryFolderPath =
        repositoryFolderPath.replace("~/", System.getProperty("user.home") + "/");
    this.typeDeclarations = typeDeclarations;
    this.urlAlias = urlAlias;
  }

  public Defaults getDefaults() {
    return defaults;
  }

  public int getOffsetForAlternativePaging() {
    return offsetForAlternativePaging;
  }

  public String getRepositoryFolderPath() {
    return repositoryFolderPath;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "partially filled at runtime")
  public TypeDeclarations getTypeDeclarations() {
    return typeDeclarations;
  }

  public UrlAlias getUrlAlias() {
    return new UrlAlias(urlAlias);
  }

  public static class Defaults {

    private String language;
    private Locale locale;

    @JsonCreator(mode = Mode.PROPERTIES)
    public Defaults(
        @JsonProperty(value = "language") String language,
        @JsonProperty(value = "locale") Locale locale) {
      if (!StringUtils.hasText(language)) {
        throw new IllegalStateException(
            "Required `cudami.defaults.language` configuration missing.");
      }
      this.language = language;
      if (!StringUtils.hasText(locale.getLanguage())) {
        throw new IllegalStateException("Required `cudami.defaults.locale` configuration missing.");
      }
      this.locale = locale;
    }

    public String getLanguage() {
      return language;
    }

    public Locale getLocale() {
      return locale;
    }
  }

  public static class UrlAlias {

    private static final int DB_MAX_LENGTH = 256;

    private List<String> generationExcludes;
    private int maxLength = -1;

    public UrlAlias() {}

    @SuppressFBWarnings(
        value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION",
        justification = "Application must not start with an invalid configuration")
    @JsonCreator(mode = Mode.PROPERTIES)
    public UrlAlias(
        @JsonProperty(value = "generationExcludes") List<String> generationExcludes,
        @JsonProperty(value = "maxLength") int maxLength) {
      this.generationExcludes =
          generationExcludes != null ? List.copyOf(generationExcludes) : Collections.EMPTY_LIST;
      if (maxLength > DB_MAX_LENGTH) {
        throw new RuntimeException(
            "The maxLength you configured is invalid, because it is greater than "
                + DB_MAX_LENGTH
                + " (this is the greatest possible length in the database)!");
      }
      this.maxLength = maxLength;
    }

    public UrlAlias(UrlAlias other) {
      this.generationExcludes = other.generationExcludes;
      this.maxLength = other.maxLength;
    }

    public List<String> getGenerationExcludes() {
      return List.copyOf(generationExcludes);
    }

    public void setGenerationExcludes(List<String> generationExcludes) {
      this.generationExcludes = generationExcludes != null ? List.copyOf(generationExcludes) : null;
    }

    public int getMaxLength() {
      return maxLength;
    }

    public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
    }
  }
}
