package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import java.util.Arrays;
import java.util.Iterator;

public abstract class AbstractPagingAndSortingRepositoryImpl {

  private void addLimit(PageRequest pageRequest, StringBuilder query) {
    int pageSize = pageRequest.getPageSize();
    if (pageSize > 0) {
      query.append(" LIMIT ").append(pageSize);
    }
  }

  private void addOffset(PageRequest pageRequest, StringBuilder query) {
    int offset = pageRequest.getOffset();
    if (offset >= 0) {
      query.append(" OFFSET ").append(offset);
    }
  }

  private void addOrderBy(PageRequest pageRequest, StringBuilder query, String[] allowedOrderByFields) {
    // Sorting
    String sortDirection = null;
    String sortField = null;
    Sorting sorting = pageRequest.getSorting();
    if (sorting != null) {
      Iterator<Order> iterator = sorting.iterator();
      if (iterator.hasNext()) {
        Order order = iterator.next();
        sortField = order.getProperty();
        if (sortField != null) {
          Direction direction = order.getDirection();
          if (direction != null && direction.isDescending()) {
            sortDirection = " DESC";
          }
        }
      }
    }
    if (sortField == null) {
      sortField = "id";
    }

    if ("id".equals(sortField) || (allowedOrderByFields != null && Arrays.asList(allowedOrderByFields).contains(sortField))) {
      // Do not just append sortFiels value (check if is in allowed fields or equals "id")
      // binding of jdbi/database does not work for order by!!!
      query.append(" ORDER BY ").append(sortField);
      if (sortDirection == null) {
        sortDirection = " ASC";
      }
      query.append(sortDirection);
    }
  }

  protected void addPageRequestParams(PageRequest pageRequest, StringBuilder query) {
    addOrderBy(pageRequest, query, getAllowedOrderByFields());
    addLimit(pageRequest, query);
    addOffset(pageRequest, query);
  }

  protected abstract String[] getAllowedOrderByFields();
}
