package de.digitalcollections.model.jackson.mixin.list.filtering;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import java.util.Collection;

@JsonTypeName("FILTERCRITERION")
public abstract class FilterCriterionMixIn extends FilterCriterion<Object> {

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "className")
  private Comparable<?> maxValue;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "className")
  private Comparable<?> minValue;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "className")
  private Object value;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "className")
  private Collection<?> values;

  @JsonCreator
  public FilterCriterionMixIn(
      @JsonProperty("expression") String expression,
      @JsonProperty("nativeExpression") boolean nativeExpression,
      @JsonProperty("operation") FilterOperation operation,
      @JsonProperty("value") Object value,
      @JsonProperty("minValue") Comparable<?> minValue,
      @JsonProperty("maxValue") Comparable<?> maxValue,
      @JsonProperty("values") Collection<?> values) {
    super(
        expression,
        nativeExpression,
        operation,
        value,
        (Comparable<Object>) minValue,
        (Comparable<Object>) maxValue,
        (Collection<Object>) values);
  }
}
