package de.digitalcollections.model.jackson.mixin.text;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Collection;
import java.util.Locale;

@JsonDeserialize(as = LocalizedText.class)
public interface LocalizedTextMixIn {

  @JsonIgnore
  Collection<String> getLanguages();

  @JsonIgnore
  Collection<Locale> getLocales();

  @JsonIgnore
  String getText();

  @JsonIgnore
  String has();
}
