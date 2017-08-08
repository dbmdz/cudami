package de.digitalcollections.cudami.client.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class UserJsonFilter {

  @JsonIgnore
  abstract String getPasswordHash();
}
