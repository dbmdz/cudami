package de.digitalcollections.cudami.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CudamiConfig {
  private Defaults defaults;
  private UrlAlias urlAlias;
  private int offsetForAlternativePaging = 0;

  @JsonCreator(mode = Mode.PROPERTIES)
  public CudamiConfig(
      @JsonProperty(value = "defaults") Defaults defaults,
      @JsonProperty(value = "urlAlias") UrlAlias urlAlias,
      @JsonProperty(value = "offsetForAlternativePaging") int offsetForAlternativePaging) {
    this.defaults = defaults;
    this.urlAlias = urlAlias;
    this.offsetForAlternativePaging = offsetForAlternativePaging;
  }

  public Defaults getDefaults() {
    return defaults;
  }

  public UrlAlias getUrlAlias() {
    return urlAlias;
  }

  public int getOffsetForAlternativePaging() {
    return offsetForAlternativePaging;
  }

  public static class Defaults {
    private String language;
    private Locale locale;

    @JsonCreator(mode = Mode.PROPERTIES)
    public Defaults(
        @JsonProperty(value = "language") String language,
        @JsonProperty(value = "locale") Locale locale) {
      this.language = language;
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

    public List<String> getGenerationExcludes() {
      return List.copyOf(generationExcludes);
    }

    public int getMaxLength() {
      return maxLength;
    }
  }
}
