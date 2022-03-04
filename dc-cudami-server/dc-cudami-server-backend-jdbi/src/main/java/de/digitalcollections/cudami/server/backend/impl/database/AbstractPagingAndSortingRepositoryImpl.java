package de.digitalcollections.cudami.server.backend.impl.database;

import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Convenience repository implementation to be inherited from if applicable.
 *
 * <p>Tries best to translate paging and sorting params into valid SQL.<br>
 * If result does not fit your use case: implement it yourself and do not use these convenience
 * methods.
 */
public abstract class AbstractPagingAndSortingRepositoryImpl {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractPagingAndSortingRepositoryImpl.class);

  private void addLimit(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest != null) {
      int pageSize = pageRequest.getPageSize();
      if (pageSize > 0) {
        sqlQuery.append(" ").append("LIMIT").append(" ").append(pageSize);
      }
    }
  }

  private void addOffset(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest != null) {
      int offset = pageRequest.getOffset();
      if (offset >= 0) {
        sqlQuery.append(" ").append("OFFSET").append(" ").append(offset);
      }
    }
  }

  protected void addOrderBy(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest != null) {
      // Sorting
      Sorting sorting = pageRequest.getSorting();
      String orderBy = getOrderBy(sorting);
      if (StringUtils.hasText(orderBy)) {
        if (!sqlQuery.toString().matches("(?i).* order by .*")) {
          sqlQuery.append(" ORDER BY ");
        }
        sqlQuery.append(orderBy);
      }
    }
  }

  protected void addPageRequestParams(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest != null) {
      addOrderBy(pageRequest, sqlQuery);
      addLimit(pageRequest, sqlQuery);
      addOffset(pageRequest, sqlQuery);
    }
  }

  /**
   * @return model properties names that are applicable for sorting, will be mapped to database
   *     column names using @see #getColumnName
   */
  protected abstract List<String> getAllowedOrderByFields();

  /**
   * @param modelProperty name of model property passed as String, e.g. "lastModified"
   * @return full qualified column name as used in sql queries ("last_modified" or e.g.
   *     "w.last_modified" if prefix used in queries)
   */
  protected abstract String getColumnName(String modelProperty);

  public String getOrderBy(Sorting sorting) {
    if (sorting == null) {
      return null;
    }
    List<String> allowedOrderByFields = getAllowedOrderByFields();
    if (getUniqueField() != null && !allowedOrderByFields.contains(getUniqueField())) {
      allowedOrderByFields.add(getUniqueField());
    }
    String orderBy =
        Optional.ofNullable(sorting.getOrders()).orElse(Collections.emptyList()).stream()
            .filter(
                o -> {
                  String sortField = o.getProperty();
                  final boolean allowedSortField =
                      sortField != null
                          && allowedOrderByFields != null
                          && allowedOrderByFields.contains(sortField);
                  if (!allowedSortField) {
                    LOGGER.warn("'" + sortField + "' not in allowed sort fields! Ignoring it.");
                  }
                  return allowedSortField;
                })
            .map(
                o -> {
                  String sortDirection = null;
                  Direction direction = o.getDirection();
                  if (direction != null && direction.isDescending()) {
                    sortDirection = "DESC";
                  } else {
                    sortDirection = "ASC";
                  }
                  String sortField = o.getProperty();
                  Optional<String> subSortField = o.getSubProperty();
                  String fullQualifiedColumnName = getColumnName(sortField);
                  if (subSortField.isEmpty()) {
                    return String.format("%s %s", fullQualifiedColumnName, sortDirection);
                  }
                  return String.format(
                      "%s->>'%s' %s", fullQualifiedColumnName, subSortField.get(), sortDirection);
                })
            .collect(Collectors.joining(","));
    return orderBy;
  }

  /**
   * @return name of model property that guarantees an unique sorting, e.g. a db primary key or
   *     another unique column/field
   */
  protected abstract String getUniqueField();
}
