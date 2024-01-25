package de.digitalcollections.model.list.buckets;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;

public class BucketObjectsResponse<T extends UniqueObject> extends PageResponse<T> {

  public BucketObjectsResponse() {
    super();
  }

  public BucketObjectsResponse(BucketObjectsRequest<T> bucketObjectsRequest, List<T> content) {
    this(bucketObjectsRequest, content, content.size());
  }

  public BucketObjectsResponse(
      BucketObjectsRequest<T> bucketObjectsRequest, List<T> content, long total) {
    super(content, bucketObjectsRequest, total);
  }
}
