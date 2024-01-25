package de.digitalcollections.model.list.paging;

import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.Iterator;

/** Container (DTO) for find parameters. */
public class FindParams {

  final String nullHandling;

  final int pageNumber;
  final int pageSize;
  final String sortDirection;
  final String sortField;

  public FindParams(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    Sorting sorting = pageRequest.getSorting();
    Iterator<Order> iterator = sorting.iterator();

    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";

    if (iterator.hasNext()) {
      Order order = iterator.next();
      sortField = order.getProperty() == null ? "" : order.getProperty();
      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
    }

    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.sortField = sortField;
    this.sortDirection = sortDirection;
    this.nullHandling = nullHandling;
  }

  public FindParams(
      int pageNumber, int pageSize, String sortField, String sortDirection, String nullHandling) {
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.sortField = sortField;
    this.sortDirection = sortDirection;
    this.nullHandling = nullHandling;
  }

  public String getNullHandling() {
    return nullHandling;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public String getSortField() {
    return sortField;
  }
}
