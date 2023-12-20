package de.digitalcollections.model.jackson.mixin.list.buckets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.buckets.BucketsRequest;

@JsonDeserialize(as = BucketsRequest.class)
public abstract class BucketsRequestMixIn {}
