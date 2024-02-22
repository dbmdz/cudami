package io.github.dbmdz.cudami.admin.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GrantedAuthorityJsonFilter {

  @JsonIgnore
  abstract String getAuthority();
}
