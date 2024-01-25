package de.digitalcollections.model.jackson.mixin.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.util.List;

@JsonDeserialize(as = User.class)
public interface UserMixIn extends UniqueObjectMixIn {

  @JsonInclude(value = Include.NON_NULL)
  public List<Role> getRoles();
}
