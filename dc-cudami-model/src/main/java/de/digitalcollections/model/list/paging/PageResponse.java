package de.digitalcollections.model.list.paging;

import de.digitalcollections.model.list.ListResponse;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for pagination information. See Spring Data Commons, but more flat design and
 * independent of Spring libraries.
 *
 * @param <T> object type listed in page
 */
public class PageResponse<T> extends ListResponse<T, PageRequest> {

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Class c) {
    return new Builder(c);
  }

  public PageResponse() {
    super();
    init();
  }

  /**
   * Creates a new {@link PageResponse} with the given content. This will result in the created
   * {@link PageResponse} being identical to the entire {@link List}.
   *
   * @param content must not be {@literal null}.
   */
  public PageResponse(List<T> content) {
    this(content, null, null == content ? 0 : content.size());
  }

  /**
   * Constructor of {@code PageResponse} with the given content and the given governing {@link
   * PageRequest}.
   *
   * @param content the content of this page, must not be {@literal null}.
   * @param request the paging information, can be {@literal null}.
   * @param total the total amount of items available. The total might be adapted considering the
   *     length of the content given, if it is going to be the content of the last page. This is in
   *     place to mitigate inconsistencies
   */
  public PageResponse(List<T> content, PageRequest request, long total) {
    this(content, request, total, null);
  }

  /**
   * Constructor of {@code PageResponse} with the given content and the given governing {@link
   * PageRequest}.
   *
   * @param content the content of this page, must not be {@literal null}.
   * @param request the paging information, can be {@literal null}.
   * @param total the total amount of items available. The total might be adapted considering the
   *     length of the content given, if it is going to be the content of the last page. This is in
   *     place to mitigate inconsistencies
   * @param executedSearchTerm search term being effectively used (may bechanged/normalized in
   *     comparance to original sent request search term) on server side for some reason
   */
  public PageResponse(List<T> content, PageRequest request, long total, String executedSearchTerm) {
    super(content, request, executedSearchTerm);

    if (content == null) {
      throw new RuntimeException("Content must not be null!");
    }

    this.total =
        !content.isEmpty() && request != null && request.getOffset() + request.getPageSize() > total
            ? request.getOffset() + content.size()
            : total;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PageResponse<?>)) {
      return false;
    }
    return super.equals(obj);
  }

  /**
   * Utility method for creating a paging navigation.
   *
   * <p>Based on the current page number a list (of maximum length of maxNumberOfItems) of
   * navigation items is returned.
   *
   * <p>The current page should be in the middle of the returned PageItems if possible (having
   * enough previous and next pages).
   *
   * <p>Each navigation item ({@link PageItem} contains a number (= label) and a flag if it is the
   * current page.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>current page is 1 (of e.g. 202 total pages), given maxNumberOfItems is 5: returning 5
   *       page items for pages: [1], 2, 3, 4, 5
   *   <li>current page is 17 (of e.g. 202 total pages), given maxNumberOfItems is 5: returning 5
   *       page items for pages: 15, 16, [17], 18, 19
   *   <li>current page is 201 (of e.g. 202 total pages), given maxNumberOfItems is 5: returning 4
   *       page items for pages: 199, 200, [201], 202
   * </ul>
   *
   * @param maxNumberOfItems maximum number of returned navigation items
   * @return list of navigation items of type {@link PageItem}
   */
  public List<PageItem> getNavItems(int maxNumberOfItems) {
    List<PageItem> items = new ArrayList<>(maxNumberOfItems);

    int firstPageNumber;
    int numberOfItems;

    int currentPageNumber = getPageNumber() + 1;
    if (getTotalPages() <= maxNumberOfItems) {
      firstPageNumber = 1;
      numberOfItems = getTotalPages();
    } else if (currentPageNumber <= (maxNumberOfItems - (maxNumberOfItems / 2))) {
      firstPageNumber = 1;
      numberOfItems = maxNumberOfItems;
    } else if (currentPageNumber >= getTotalPages() - maxNumberOfItems / 2) {
      firstPageNumber = getTotalPages() - maxNumberOfItems + 1;
      numberOfItems = maxNumberOfItems;
    } else {
      firstPageNumber = currentPageNumber - maxNumberOfItems / 2;
      numberOfItems = maxNumberOfItems;
    }

    for (int i = 0; i < numberOfItems; i++) {
      int number = firstPageNumber + i;
      boolean isCurrent = (number == currentPageNumber);
      items.add(new PageItem(number, isCurrent));
    }

    return items;
  }

  /**
   * Returns the number of elements currently on this {@link PageResponse}.
   *
   * @return the number of elements currently on this {@link PageResponse}.
   */
  public int getNumberOfElements() {
    return content.size();
  }

  /**
   * Returns the number of the current {@link PageResponse}. Is always non-negative.
   *
   * @return the number of the current {@link PageResponse}.
   */
  public int getPageNumber() {
    return request == null ? 0 : request.getPageNumber();
  }

  /**
   * Returns the size of the {@link PageResponse}.
   *
   * @return the size of the {@link PageResponse}.
   */
  public int getSize() {
    return request == null ? 0 : request.getPageSize();
  }

  /**
   * Returns the number of total pages.
   *
   * @return the number of total pages
   */
  public int getTotalPages() {
    return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
  }

  /**
   * Returns if there is a next {@link PageResponse}.
   *
   * @return if there is a next {@link PageResponse}.
   */
  public boolean hasNext() {
    return getPageNumber() + 1 < getTotalPages();
  }

  /**
   * Returns if there is a previous {@link PageResponse}.
   *
   * @return if there is a previous {@link PageResponse}.
   */
  public boolean hasPrevious() {
    return getPageNumber() > 0;
  }

  @Override
  protected void init() {
    super.init();
  }

  /**
   * Returns whether the current {@link PageResponse} is the first one.
   *
   * @return whether the current {@link PageResponse} is the first one.
   */
  public boolean isFirst() {
    return !hasPrevious();
  }

  /**
   * Returns whether the current {@link PageResponse} is the last one.
   *
   * @return whether the current {@link PageResponse} is the last one.
   */
  public boolean isLast() {
    return !hasNext();
  }

  /**
   * Returns the {@link PageRequest} to request the next {@link PageResponse}. Can be {@literal
   * null} in case the current {@link PageResponse} is already the last one. Clients should check
   * {@link #hasNext()} before calling this method to make sure they receive a non-{@literal null}
   * value.
   *
   * @return the {@link PageRequest} to request the next {@link PageResponse}
   */
  public PageRequest nextPageRequest() {
    return hasNext() ? request.next() : null;
  }

  /**
   * Returns the {@link PageRequest} to request the previous {@link PageResponse}. Can be {@literal
   * null} in case the current {@link PageResponse} is already the first one. Clients should check
   * {@link #hasPrevious()} before calling this method make sure receive a non-{@literal null}
   * value.
   *
   * @return the {@link PageRequest} to request the previous {@link PageResponse}
   */
  public PageRequest previousPageRequest() {
    if (hasPrevious()) {
      return request.previousOrFirst();
    }
    return null;
  }

  @Override
  public String toString() {

    String contentType = "UNKNOWN";
    List<T> unmodifiableContent = getContent();

    if (!unmodifiableContent.isEmpty()) {
      contentType = unmodifiableContent.get(0).getClass().getName();
    }

    return String.format(
        "Page %s of %d containing %s instances", getPageNumber() + 1, getTotalPages(), contentType);
  }

  public static class Builder<T, B extends PageResponse<T>, C extends Builder> {

    List<FilterCriterion> filterCriteria;
    List<Order> orders;
    PageRequest request = new PageRequest();
    B pageResponse;

    public Builder() {
      pageResponse = (B) new PageResponse<T>();
    }

    public Builder(Class<T> clazz) {
      pageResponse = (B) new PageResponse<T>();
    }

    public B build() {
      if (filterCriteria != null && !filterCriteria.isEmpty()) {
        request.setFiltering(new Filtering(filterCriteria));
      }

      if (orders != null && !orders.isEmpty()) {
        Sorting sorting = new Sorting();
        sorting.setOrders(orders);
        request.setSorting(sorting);
      }

      if (pageResponse.getTotalElements() == 0
          && pageResponse.getContent() != null
          && !pageResponse.getContent().isEmpty()) {
        pageResponse.setTotalElements(pageResponse.getContent().size());
      }

      pageResponse.setRequest(request);
      return pageResponse;
    }

    public C forAscendingOrderedField(String expression, String subfield) {
      Order order =
          Order.builder()
              .direction(Direction.ASC)
              .property(expression)
              .subProperty(subfield)
              .build();
      if (orders == null) {
        orders = new ArrayList<>(0);
      }
      orders.add(order);
      return (C) this;
    }

    public C forAscendingOrderedField(String expression) {
      return forAscendingOrderedField(expression, "");
    }

    public C forDescendingOrderedField(String expression, String subfield) {
      Order order =
          Order.builder()
              .direction(Direction.DESC)
              .property(expression)
              .subProperty(subfield)
              .build();
      if (orders == null) {
        orders = new ArrayList<>(0);
      }
      orders.add(order);
      return (C) this;
    }

    public C forDescendingOrderedField(String expression) {
      return forDescendingOrderedField(expression, "");
    }

    public C forEndDate(String expression, String endDate) {
      FilterCriterion filterCriterionEnd =
          new FilterCriterion(
              expression, FilterOperation.GREATER_THAN_OR_NOT_SET, LocalDate.parse(endDate));
      if (filterCriteria == null) {
        filterCriteria = new ArrayList<>(0);
      }
      filterCriteria.add(filterCriterionEnd);
      return (C) this;
    }

    public C forEqualPredicate(String expression, String predicate) {
      if (filterCriteria == null) {
        filterCriteria = new ArrayList<>(0);
      }
      filterCriteria.add(new FilterCriterion(expression, FilterOperation.EQUALS, predicate));
      return (C) this;
    }

    public C forPageSize(int pageSize) {
      request.setPageSize(pageSize);
      return (C) this;
    }

    public C forRequestPage(int requestPage) {
      request.setPageNumber(requestPage);
      return (C) this;
    }

    public C forStartDate(String expression, String startDate) {
      FilterCriterion filterCriterionStart =
          new FilterCriterion(
              expression,
              FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET,
              LocalDate.parse(startDate));
      if (filterCriteria == null) {
        filterCriteria = new ArrayList<>(0);
      }
      filterCriteria.add(filterCriterionStart);
      return (C) this;
    }

    public C withContent(List<T> content) {
      pageResponse.setContent(content);
      return (C) this;
    }

    public C withContent(T content) {
      pageResponse.setContent(List.of(content));
      return (C) this;
    }

    public C withExecutedSearchTerm(String executedSearchTerm) {
      pageResponse.setExecutedSearchTerm(executedSearchTerm);
      return (C) this;
    }

    public C withTotalElements(long totalElements) {
      pageResponse.setTotalElements(totalElements);
      return (C) this;
    }

    public C withoutContent() {
      pageResponse.setTotalElements(0);
      pageResponse.setContent(List.of());
      return (C) this;
    }
  }
}
