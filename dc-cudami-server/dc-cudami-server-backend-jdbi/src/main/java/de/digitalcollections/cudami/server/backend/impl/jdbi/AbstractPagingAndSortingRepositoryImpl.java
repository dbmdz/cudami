package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Convenience repository implementation to be inherited from if applicable.
 *
 * <p>
 * Tries best to translate paging and sorting params into valid SQL.<br>
 * If result does not fit your use case: implement it yourself and do not use these convenience methods.</p>
 */
public abstract class AbstractPagingAndSortingRepositoryImpl {

  public void addLimit(PageRequest pageRequest, StringBuilder query) {
    int pageSize = pageRequest.getPageSize();
    if (pageSize > 0) {
      query.append(" ").append("LIMIT").append(" ").append(pageSize);
    }
  }

  public void addOffset(PageRequest pageRequest, StringBuilder query) {
    int offset = pageRequest.getOffset();
    if (offset >= 0) {
      query.append(" ").append("OFFSET").append(" ").append(offset);
    }
  }

  public void addOrderBy(PageRequest pageRequest, StringBuilder query) {
    String[] allowedOrderByFields = getAllowedOrderByFields();

    // Sorting
    Sorting sorting = pageRequest.getSorting();
    if (sorting != null) {
      Iterator<Order> iterator = sorting.iterator();
      if (iterator.hasNext()) {
        // TODO only one sort field supported by now...
        Order order = iterator.next();
        String sortField = order.getProperty();
        String sortDirection = null;
        if (sortField != null) {
          if (allowedOrderByFields != null && Arrays.asList(allowedOrderByFields).contains(sortField)) {
            String fullQualifiedColumnName = getColumnName(sortField);
            Direction direction = order.getDirection();
            if (direction != null && direction.isDescending()) {
              sortDirection = "DESC";
            } else {
              sortDirection = "ASC";
            }
            query.append(" ").append("ORDER BY").append(" ").append(fullQualifiedColumnName).append(" ").append(sortDirection);
          }
        }
      }
    }
  }

  protected void addPageRequestParams(PageRequest pageRequest, StringBuilder query) {
    if (pageRequest != null) {
      addOrderBy(pageRequest, query);
      addLimit(pageRequest, query);
      addOffset(pageRequest, query);
    }
  }

  /**
   * full qualified column names in database table that are applicable for sorting
   *
   * @return full qualified column name as used in sql queries ("last_modified" or e.g. "w.last_modified" if prefix used in queries)
   */
  protected abstract String[] getAllowedOrderByFields();

  /**
   * full qualified column name in database table may vary from property name in model object
   *
   * @param modelProperty name of model property passed as String, e.g. "lastModified"
   * @return column name as used in sql queries ("last_modified" or e.g. "w.last_modified" if prefix used in queries)
   */
  protected abstract String getColumnName(String modelProperty);
}
