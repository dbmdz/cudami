package de.digitalcollections.cudami.model.jackson.mixin.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.resource.ContentNodeImpl;

@JsonDeserialize(as = ContentNodeImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("contentNode")
public interface ContentNodeMixIn {

}
