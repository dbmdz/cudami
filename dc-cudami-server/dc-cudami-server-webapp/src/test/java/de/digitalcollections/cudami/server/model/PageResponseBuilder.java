package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PageResponseBuilder<T, B extends PageResponse<T>, C extends PageResponseBuilder> {

  B pageResponse;
  PageRequest pageRequest = new PageRequest();
  List<FilterCriterion> filterCriteria;
  List<Order> orders;

  public PageResponseBuilder() {
    pageResponse = (B) new PageResponse<T>();
  }

  public PageResponseBuilder(Class<T> clazz) {
    pageResponse = (B) new PageResponse<T>();
  }

  public B build() {
    if (filterCriteria != null && !filterCriteria.isEmpty()) {
      pageRequest.setFiltering(new Filtering(filterCriteria));
    }

    if (orders != null && !orders.isEmpty()) {
      Sorting sorting = new Sorting();
      sorting.setOrders(orders);
      pageRequest.setSorting(sorting);
    }

    if (pageResponse.getTotalElements() == 0
        && pageResponse.getContent() != null
        && !pageResponse.getContent().isEmpty()) {
      pageResponse.setTotalElements(pageResponse.getContent().size());
    }

    pageResponse.setPageRequest(pageRequest);
    return pageResponse;
  }

  public C withoutContent() {
    pageResponse.setTotalElements(0);
    pageResponse.setContent(List.of());
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

  public C forRequestPage(int requestPage) {
    pageRequest.setPageNumber(requestPage);
    return (C) this;
  }

  public C forPageSize(int pageSize) {
    pageRequest.setPageSize(pageSize);
    return (C) this;
  }

  public C forStartDate(String expression, String startDate) {
    FilterCriterion filterCriterionStart =
        new FilterCriterion(
            expression, FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET, LocalDate.parse(startDate));
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(filterCriterionStart);
    return (C) this;
  }

  public C forEndDate(String expression, String endDate) {
    FilterCriterion filterCriterionEnd =
        new FilterCriterion(
            expression, FilterOperation.GREATER_THAN_OR_NOT_SET, LocalDate.parse(endDate));
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(filterCriterionEnd);
    return (C) this;
  }

  public C forAscendingOrderedField(String expression, String subfield) {
    Order order =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property(expression)
            .subProperty(subfield)
            .build();
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders.add(order);
    return (C) this;
  }

  public C forDescendingOrderedField(String expression, String subfield) {
    Order order =
        new OrderBuilder()
            .direction(Direction.DESC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property(expression)
            .subProperty(subfield)
            .build();
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders.add(order);
    return (C) this;
  }

  public C forEqualPredicate(String expression, String predicate) {
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(new FilterCriterion(expression, FilterOperation.EQUALS, predicate));
    return (C) this;
  }

  public C forAscendingOrderedField(String expression) {
    return forAscendingOrderedField(expression, "");
  }

  public C forDescendingOrderedField(String expression) {
    return forDescendingOrderedField(expression, "");
  }

  public C withTotalElements(long totalElements) {
    pageResponse.setTotalElements(totalElements);
    return (C) this;
  }
}
