package de.digitalcollections.cudami.model.jackson.mixin.identifiable.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.resource.ContentBlocksContainerImpl;

@JsonDeserialize(as = ContentBlocksContainerImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ContentBlocksContainerMixIn {

}
