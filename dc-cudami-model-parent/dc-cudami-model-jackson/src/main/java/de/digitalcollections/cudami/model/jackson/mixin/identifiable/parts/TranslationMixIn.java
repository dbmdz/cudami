package de.digitalcollections.cudami.model.jackson.mixin.identifiable.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TranslationImpl;

@JsonDeserialize(as = TranslationImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TranslationMixIn {

}
