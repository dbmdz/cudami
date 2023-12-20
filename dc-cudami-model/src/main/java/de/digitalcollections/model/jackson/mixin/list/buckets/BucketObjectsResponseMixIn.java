package de.digitalcollections.model.jackson.mixin.list.buckets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.jackson.mixin.list.paging.PageResponseMixIn;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;

@JsonDeserialize(as = BucketObjectsResponse.class)
public abstract class BucketObjectsResponseMixIn<T extends UniqueObject>
    extends PageResponseMixIn<T> {

  @JsonTypeInfo(use = Id.NAME, property = "objectType", visible = true)
  @JsonSubTypes({@Type(value = Headword.class, name = "HEADWORD")})
  @Override
  public abstract List<T> getContent();
}
