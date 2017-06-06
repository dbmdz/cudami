package de.digitalcollections.cms.client.webapp.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class UserJsonFilter {

  @JsonIgnore
  abstract String getPasswordHash();
}
