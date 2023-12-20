package de.digitalcollections.model.list.paging;

import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

/**
 * Container for paging, sorting, filtering and searching params:
 *
 * <ul>
 *   <li>pageNumber: which page to be returned
 *   <li>pageSize: how many items on one page
 *   <li>sorting: container for sorting order of result list
 *   <li>filtering: container for filter criterias of result list
 *   <li>searchTerm: searchTerm term for simple query term to be searched for
 * </ul>
 */
public class PageRequest extends ListRequest {

  public static Builder builder() {
    return new Builder();
  }

  private int pageNumber;
  private int pageSize;

  public PageRequest() {
    super();
    init();
  }

  /**
   * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page}
   * will return the first page.
   *
   * @param pageNumber zero-based page index.
   * @param pageSize the size of the page to be returned.
   */
  public PageRequest(int pageNumber, int pageSize) {
    this(pageNumber, pageSize, null, null, null);
  }

  /**
   * Creates a new {@link PageRequest} with sorting parameters applied.
   *
   * @param pageNumber zero-based page index.
   * @param pageSize the size of the page to be returned.
   * @param direction the direction of the {@link Sorting} to be specified, can be {@literal null}.
   * @param properties the properties to sorting by, must not be {@literal null} or empty.
   */
  public PageRequest(int pageNumber, int pageSize, Direction direction, String... properties) {
    this(pageNumber, pageSize, new Sorting(direction, properties), null, null);
  }

  public PageRequest(int pageNumber, int pageSize, Sorting sorting) {
    this(pageNumber, pageSize, sorting, null, null);
  }

  public PageRequest(int pageNumber, int pageSize, Sorting sorting, Filtering filtering) {
    this(pageNumber, pageSize, sorting, filtering, null);
  }

  public PageRequest(String searchTerm, int pageNumber, int pageSize) {
    this(pageNumber, pageSize, null, null, searchTerm);
  }

  public PageRequest(String searchTerm, int pageNumber, int pageSize, Sorting sorting) {
    this(pageNumber, pageSize, sorting, null, searchTerm);
  }

  /**
   * Creates a new {@link PageRequest} with sorting parameters applied.
   *
   * @param pageNumber zero-based page index, must not be less than zero.
   * @param pageSize the size of the page to be returned, must not be less than one.
   * @param sorting can be {@literal null}
   * @param filtering contains list of filter criterias
   * @param searchTerm searchTerm term for simple query term to be searched for
   */
  public PageRequest(
      int pageNumber, int pageSize, Sorting sorting, Filtering filtering, String searchTerm) {
    super(sorting, filtering, searchTerm);
    init();

    if (pageNumber < 0) {
      throw new IllegalArgumentException("Page index must not be less than zero!");
    }

    if (pageSize < 1) {
      throw new IllegalArgumentException("Page size must not be less than one!");
    }
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
  }

  public PageRequest(int pageNumber, int pageSize, List<Order> sortBy) {
    this(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      setSorting(sorting);
    }
  }

  @SuppressFBWarnings(
      value = "EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC",
      justification = "to be reviewed later")
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof PageRequest)) {
      return false;
    }

    PageRequest that = (PageRequest) obj;

    boolean searchTermEqual =
        (this.searchTerm == null
            ? that.searchTerm == null
            : this.searchTerm.equals(that.searchTerm));
    boolean filterEqual =
        (this.filtering == null ? that.filtering == null : this.filtering.equals(that.filtering));
    boolean sortEqual =
        (this.sorting == null ? that.sorting == null : this.sorting.equals(that.sorting));
    boolean othersEqual = (this.pageNumber == that.pageNumber && this.pageSize == that.pageSize);

    return searchTermEqual && filterEqual && othersEqual && sortEqual;
  }

  /**
   * @return the {@link PageRequest} requesting the first page
   */
  public PageRequest first() {
    return new PageRequest(0, getPageSize(), getSorting(), getFiltering(), getSearchTerm());
  }

  /**
   * @return the offset to be taken according to the underlying page and page size.
   */
  public int getOffset() {
    return pageNumber * pageSize;
  }

  /**
   * @return the page to be returned.
   */
  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * @return the number of items of that page
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @return the search term to be searched for
   */

  /**
   * Returns whether there's a previous {@link PageRequest} we can access from the current one. Will
   * return {@literal false} in case the current {@link PageRequest} already refers to the first
   * page.
   *
   * @return whether there's a previous PageRequest
   */
  public boolean hasPrevious() {
    return pageNumber > 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + pageNumber;
    result = prime * result + pageSize;

    return 31 * result
        + (null == sorting ? 0 : sorting.hashCode())
        + (null == filtering ? 0 : filtering.hashCode());
  }

  @Override
  protected void init() {
    super.init();
  }

  /**
   * @return the {@link PageRequest} requesting the next page
   */
  public PageRequest next() {
    return new PageRequest(
        getPageNumber() + 1, getPageSize(), getSorting(), getFiltering(), getSearchTerm());
  }

  /**
   * Returns the {@link PageResponse} requesting the previous page.
   *
   * @return the PageResponse requesting the previous page
   */
  public PageRequest previous() {
    return getPageNumber() == 0
        ? this
        : new PageRequest(
            getPageNumber() - 1, getPageSize(), getSorting(), getFiltering(), getSearchTerm());
  }

  /**
   * @return the previous {@link PageRequest} or the first {@link PageRequest} if the current one
   *     already is the first one
   */
  public PageRequest previousOrFirst() {
    return hasPrevious() ? previous() : first();
  }

  /**
   * @param pageNumber the page to be returned
   */
  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  /**
   * @param pageSize the number of items of that page
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  @Override
  public String toString() {
    return String.format(
        "Page request [number: %d, size %d, sorting: %s, filtering: %s, searchTerm: %s]",
        getPageNumber(),
        getPageSize(),
        sorting == null ? null : sorting.toString(),
        filtering == null ? null : filtering.toString(),
        searchTerm == null ? null : searchTerm);
  }

  public static class Builder {

    private Filtering filtering;
    private int pageNumber;
    private int pageSize;
    private String searchTerm;
    private Sorting sorting;

    public PageRequest build() {
      return new PageRequest(pageNumber, pageSize, sorting, filtering, searchTerm);
    }

    public Builder filtering(Filtering filtering) {
      this.filtering = filtering;
      return this;
    }

    public Builder pageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
      return this;
    }

    public Builder pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    public Builder searchTerm(String searchTerm) {
      this.searchTerm = searchTerm;
      return this;
    }

    public Builder sorting(Sorting sorting) {
      this.sorting = sorting;
      return this;
    }
  }
}
