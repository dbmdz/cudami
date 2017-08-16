package de.digitalcollections.cudami.model.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.ThumbnailImpl;

@JsonDeserialize(as = ThumbnailImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ThumbnailMixIn {

}
