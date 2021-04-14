package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
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

public class PageResponseBuilder<
    T extends Entity, B extends PageResponse<T>, C extends PageResponseBuilder> {

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

  public C forRequestPage(int requestPage) {
    pageRequest.setPageNumber(requestPage);
    return (C) this;
  }

  public C forPageSize(int pageSize) {
    pageRequest.setPageSize(pageSize);
    return (C) this;
  }

  public C withStartDateFilterCriterion(String fieldName, String startDate) {
    FilterCriterion filterCriterionStart =
        new FilterCriterion(
            fieldName, FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET, LocalDate.parse(startDate));
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(filterCriterionStart);
    return (C) this;
  }

  public C withEndDateFilterCriterion(String fieldName, String endDate) {
    FilterCriterion filterCriterionEnd =
        new FilterCriterion(
            fieldName, FilterOperation.GREATER_THAN_OR_NOT_SET, LocalDate.parse(endDate));
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(filterCriterionEnd);
    return (C) this;
  }

  public C withOrder(String fieldName, String subfield, Direction direction) {
    Order order =
        new OrderBuilder()
            .direction(direction)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property(fieldName)
            .subProperty(subfield)
            .build();
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders.add(order);
    return (C) this;
  }

  public C withOrder(String fieldName, Direction direction) {
    return withOrder(fieldName, "", direction);
  }

  public C withTotalElements(long totalElements) {
    pageResponse.setTotalElements(totalElements);
    return (C) this;
  }
}
