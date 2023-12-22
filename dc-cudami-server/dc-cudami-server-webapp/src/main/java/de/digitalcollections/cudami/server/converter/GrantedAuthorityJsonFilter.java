package de.digitalcollections.cudami.server.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GrantedAuthorityJsonFilter {

  @JsonIgnore
  abstract String getAuthority();
}
