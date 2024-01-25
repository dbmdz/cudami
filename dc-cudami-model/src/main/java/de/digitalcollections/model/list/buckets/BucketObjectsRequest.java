package de.digitalcollections.model.list.buckets;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;

@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "unused code")
public class BucketObjectsRequest<T extends UniqueObject> extends PageRequest {

  private static final long serialVersionUID = 1L; // FIXME: Why?

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BucketObjectsRequest)) return false;
    if (!super.equals(o)) return false;
    BucketObjectsRequest<?> that = (BucketObjectsRequest<?>) o;
    return Objects.equals(bucket, that.bucket);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), bucket);
  }
}
