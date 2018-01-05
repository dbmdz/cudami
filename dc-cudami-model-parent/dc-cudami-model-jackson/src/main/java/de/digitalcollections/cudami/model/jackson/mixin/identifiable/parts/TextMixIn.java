package de.digitalcollections.cudami.model.jackson.mixin.identifiable.parts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import java.util.Collection;

@JsonDeserialize(as = TextImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TextMixIn {

  @JsonIgnore
  Collection<String> getLanguages();

  @JsonIgnore
  String getText();
}
