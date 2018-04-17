package de.digitalcollections.cudami.admin.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class UserJsonFilter {

  @JsonIgnore
  abstract String getPasswordHash();
}
