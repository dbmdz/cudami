package io.github.dbmdz.cudami.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GrantedAuthorityJsonFilter {

  @JsonIgnore
  abstract String getAuthority();
}
