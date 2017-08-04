package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.core.model.api.paging.Order;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.core.model.api.paging.enums.Direction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractPagingAndSortingRepositoryImpl {

  protected Map<String, Object> addPageRequestParams(PageRequest pageRequest, StringBuilder query) {
    Map<String, Object> params = new HashMap<>();
    // Sorting
    String sortDirection = null;
    String sortField = null;
    Sorting sorting = pageRequest.getSorting();
    if (sorting != null) {
      Iterator<Order> iterator = sorting.iterator();
      if (iterator.hasNext()) {
        // FIXME just supporting one field sorting until now
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
    // Do not just abbend sortFiels value (could be malicious SQL injection!!!), so using binding of jdbi
    query.append(" ORDER BY :sortField");
    params.put("sortField", sortField);
    if (sortDirection == null) {
      sortDirection = " ASC";
    }
    query.append(sortDirection);
    int pageSize = pageRequest.getPageSize();
    if (pageSize > 0) {
      query.append(" LIMIT ").append(pageSize);
    }
    int offset = pageRequest.getOffset();
    if (offset >= 0) {
      query.append(" OFFSET ").append(offset);
    }
    return params;
  }

}
