package de.digitalcollections.model.jackson.mixin.list.filtering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.filtering.Filtering;

@JsonDeserialize(as = Filtering.class)
public interface FilteringMixIn {

  @JsonIgnore
  boolean isEmpty();
}
