package de.digitalcollections.cudami.model.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.entity.WebsiteImpl;

@JsonDeserialize(as = WebsiteImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("website")
public interface WebsiteMixIn {

}
