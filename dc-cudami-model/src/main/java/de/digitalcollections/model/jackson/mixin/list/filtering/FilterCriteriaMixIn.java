package de.digitalcollections.model.jackson.mixin.list.filtering;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.filtering.FilterCriteria;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import java.util.Collection;
import java.util.List;

@JsonDeserialize(as = FilterCriteria.class)
@JsonInclude(value = Include.NON_EMPTY)
// Otherwise Jackson would treat it as ARRAY and we would lose the `criterionLink`
@JsonFormat(shape = Shape.OBJECT)
public interface FilterCriteriaMixIn extends List<FilterCriterion> {

  @Override
  @JsonIgnore
  boolean isEmpty();

  /*
   * To create a valid JSON our list content needs a property name in this JSON object.
   * The following two methods (1, serialization) take the list content and put it into an array called "content"
   * and (2, deserialization) add the "content" array to this java object (that is an ArrayList) itself.
   * This might look ugly but it is more beautiful than the alternatives.
   */
  @Override
  @JsonProperty(access = Access.READ_ONLY, value = "content")
  FilterCriterion[] toArray();

  @Override
  @JsonSetter(value = "content")
  @JsonDeserialize(contentAs = FilterCriterion.class)
  boolean addAll(Collection<? extends FilterCriterion> c);
}
