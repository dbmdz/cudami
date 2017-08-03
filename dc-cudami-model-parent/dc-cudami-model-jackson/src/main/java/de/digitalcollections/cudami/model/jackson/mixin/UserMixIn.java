package de.digitalcollections.cudami.model.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.security.UserImpl;

@JsonDeserialize(as = UserImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface UserMixIn {

}
