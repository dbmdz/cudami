package de.digitalcollections.model.jackson.mixin.list.buckets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.list.ListResponseMixIn;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import java.util.List;

@JsonDeserialize(as = BucketsResponse.class)
public abstract class BucketsResponseMixIn<T, R extends BucketsRequest>
    extends ListResponseMixIn<T, R> {

  @JsonTypeInfo(use = Id.NAME, property = "objectType", visible = true)
  @JsonSubTypes({@Type(value = Bucket.class, name = "BUCKET")})
  @Override
  public abstract List<T> getContent();
}
