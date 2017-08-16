package de.digitalcollections.cudami.model.jackson.mixin.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.security.UserImpl;

@JsonDeserialize(as = UserImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("user")
public interface UserMixIn {

}
