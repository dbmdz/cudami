package de.digitalcollections.model.jackson.mixin.identifiable.alias;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import java.util.List;
import java.util.Locale;

@JsonDeserialize(as = LocalizedUrlAliases.class)
public interface LocalizedUrlAliasesMixIn {

  @JsonIgnore
  void add(UrlAlias... urlAlias);

  @JsonIgnore
  boolean containsUrlAlias(UrlAlias urlAlias);

  @JsonIgnore
  List<UrlAlias> flatten();

  @JsonIgnore
  List<Locale> getTargetLanguages();

  @JsonIgnore
  boolean hasTargetLanguage(Locale locale);
}
