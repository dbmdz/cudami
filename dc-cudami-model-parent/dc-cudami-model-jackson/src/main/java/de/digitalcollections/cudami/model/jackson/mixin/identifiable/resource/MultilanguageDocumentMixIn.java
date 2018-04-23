package de.digitalcollections.cudami.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;

@JsonDeserialize(as = MultilanguageDocumentImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MultilanguageDocumentMixIn {

}
