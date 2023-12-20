package de.digitalcollections.model.jackson.mixin.list.sorting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.sorting.Order;

@JsonDeserialize(as = Order.class)
public abstract class OrderMixIn {

  @JsonIgnore
  public abstract boolean isAscending();

  @JsonIgnore
  public abstract boolean isDescending();
}
