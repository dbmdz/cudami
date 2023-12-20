package de.digitalcollections.model.list.buckets;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Sorting;

public class BucketObjectsRequest<T extends UniqueObject> extends PageRequest {

  private static final long serialVersionUID = 1L;

  private Bucket<T> bucket;

  public BucketObjectsRequest() {
    super();
  }

  public BucketObjectsRequest(
      Bucket<T> bucket, int pageNumber, int pageSize, Sorting sorting, Filtering filtering) {
    super(pageNumber, pageSize, sorting, filtering);
    this.bucket = bucket;
  }

  public Bucket<T> getBucket() {
    return bucket;
  }

  public void setBucket(Bucket<T> bucket) {
    this.bucket = bucket;
  }
}
