package de.digitalcollections.model.list;

import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;

/**
 * Container for querying a optionally filtered and sorted list:
 *
 * <ul>
 *   <li>filtering: container for filter criterias of result list
 *   <li>sorting: container for sorting order of result list
 *   <li>searchTerm: search term for simple query term to be searched for
 * </ul>
 */
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "to be evaluated")
public class ListRequest implements Serializable {

  protected Filtering filtering;
  protected String searchTerm;
  protected Sorting sorting;

  public ListRequest() {
    init();
  }

  /**
   * Creates a new {@link ListRequest} with sorting parameters applied.
   *
   * @param direction the direction of the {@link Sorting} to be specified, can be {@literal null}.
   * @param properties the properties to sorting by, must not be {@literal null} or empty.
   */
  public ListRequest(Direction direction, String... properties) {
    this(new Sorting(direction, properties), null, null);
  }

  public ListRequest(Sorting sorting) {
    this(sorting, null, null);
  }

  /**
   * Creates a new {@link ListRequest} with sorting parameters applied.
   *
   * @param sorting can be {@literal null}
   * @param filtering contains list of filter criterias
   * @param searchTerm search term for simple query term to be searched for
   */
  public ListRequest(Sorting sorting, Filtering filtering, String searchTerm) {
    this();
    this.filtering = filtering;
    this.searchTerm = searchTerm;
    this.sorting = sorting;
  }

  /**
   * Add all filter criteria of given filtering to the existing filtering. Initialise if no existing
   * filtering.
   *
   * @param filtering new filtering criteria to add
   * @return the updated ListRequest instance
   */
  public ListRequest add(Filtering filtering) {
    Filtering existingFiltering = getFiltering();
    if (existingFiltering == null) {
      setFiltering(filtering);
    } else {
      existingFiltering.add(filtering);
    }
    return this;
  }

  /**
   * Add all sorting criteria of given sorting to existing sorting. Initialise if no existing
   * sorting.
   *
   * @param sorting new sorting criteria to add
   * @return the updated ListRequest instance
   */
  public ListRequest add(Sorting sorting) {
    Sorting existingSorting = getSorting();
    if (existingSorting == null || existingSorting.getOrders().isEmpty()) {
      setSorting(sorting);
    } else {
      existingSorting.and(sorting);
    }
    return this;
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ListRequest)) {
      return false;
    }

    ListRequest that = (ListRequest) obj;

    boolean filterEqual =
        (this.filtering == null ? that.filtering == null : this.filtering.equals(that.filtering));
    boolean sortEqual =
        (this.sorting == null ? that.sorting == null : this.sorting.equals(that.sorting));
    return filterEqual && sortEqual;
  }

  /**
   * @return the filtering parameters
   */
  public Filtering getFiltering() {
    return filtering;
  }

  /**
   * @return the search term to be searched for
   */
  public String getSearchTerm() {
    return searchTerm;
  }

  /**
   * @return the sorting parameters
   */
  public Sorting getSorting() {
    return sorting;
  }

  /**
   * @return whether the request has defined any filtering.
   */
  public boolean hasFiltering() {
    return filtering != null && !filtering.isEmpty();
  }

  /**
   * @return whether the request has defined any sorting.
   */
  public boolean hasSorting() {
    return sorting != null && sorting.getOrders() != null && !sorting.getOrders().isEmpty();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    return prime * result
        + (null == sorting ? 0 : sorting.hashCode())
        + (null == filtering ? 0 : filtering.hashCode());
  }

  protected void init() {}

  /**
   * @param filtering the filtering criterias
   */
  public void setFiltering(Filtering filtering) {
    this.filtering = filtering;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  /**
   * @param sorting the sorting parameters
   */
  public void setSorting(Sorting sorting) {
    this.sorting = sorting;
  }

  @Override
  public String toString() {
    return String.format(
        "List request [searchTerm: %s, sorting: %s, filtering: %s]",
        searchTerm == null ? null : searchTerm,
        sorting == null ? null : sorting.toString(),
        filtering == null ? null : filtering.toString());
  }
}
