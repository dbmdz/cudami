package de.digitalcollections.model.jackson.mixin.list.buckets;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.buckets.Bucket;

@JsonDeserialize(as = Bucket.class)
@JsonTypeName("BUCKET")
public interface BucketMixIn {}
