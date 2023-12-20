package de.digitalcollections.model.list.buckets;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Sorting;

/**
 * A BucketsRequest allows you to break a list / result set into a specified number of approximately
 * equal groups, or buckets.<br>
 * It assigns each group a bucket number starting from one.<br>
 * For each row in a group, the bucket number represents the group to which the row belongs.
 *
 * @param <T> type of an UniqueObject contained in a Bucket
 */
public class BucketsRequest<T extends UniqueObject> extends ListRequest {

  private static final long serialVersionUID = 1L;

  private int numberOfBuckets;
  private Bucket<T> parentBucket;

  /**
   * Creates a new {@link BucketsRequest} with sorting parameters applied.
   *
   * @param numberOfBuckets number (which is a literal positive integer number) of buckets the list
   *     should be divided into.
   * @param parentBucket bucket to be split into buckets or null if top bucket (whole list) ist to
   *     be split
   * @param sorting can be {@literal null}
   * @param filtering contains list of filter criterias
   */
  public BucketsRequest(
      int numberOfBuckets, Bucket<T> parentBucket, Sorting sorting, Filtering filtering) {
    super(sorting, filtering, null);
    if (numberOfBuckets < 1) {
      throw new IllegalArgumentException("numberOfBuckets must not be less than one!");
    }
    this.numberOfBuckets = numberOfBuckets;
    this.parentBucket = parentBucket;
  }

  /**
   * Creates a new {@link BucketsRequest} with sorting parameters applied to a previously calculated
   * (sub)bucket of all objects.
   *
   * @param numberOfBuckets number (which is a literal positive integer number) of buckets the list
   *     should be divided into.
   * @param startObject left border object (first object in source list)
   * @param endObject right border object (last object in source list)
   * @param sorting can be {@literal null}
   * @param filtering contains list of filter criterias
   */
  public BucketsRequest(
      int numberOfBuckets, T startObject, T endObject, Sorting sorting, Filtering filtering) {
    this(numberOfBuckets, new Bucket<>(startObject, endObject), sorting, filtering);
  }

  /**
   * Creates a new {@link BucketsRequest} targeting a list of objects.
   *
   * @param numberOfBuckets number (which is a literal positive integer number) of buckets the list
   *     should be divided into.
   */
  public BucketsRequest(int numberOfBuckets) {
    this(numberOfBuckets, (Bucket<T>) null, (Sorting) null, (Filtering) null);
  }

  public BucketsRequest() {}

  /**
   * @return number of buckets requested
   */
  public int getNumberOfBuckets() {
    return numberOfBuckets;
  }

  /**
   * @return parent bucket being the border for sub buckets request
   */
  public Bucket<T> getParentBucket() {
    return parentBucket;
  }

  /**
   * @param numberOfBuckets number of buckets requested
   */
  public void setNumberOfBuckets(int numberOfBuckets) {
    this.numberOfBuckets = numberOfBuckets;
  }

  /**
   * @param parentBucket parent bucket being the border for sub buckets request (or null)
   */
  public void setParentBucket(Bucket<T> parentBucket) {
    this.parentBucket = parentBucket;
  }
}
