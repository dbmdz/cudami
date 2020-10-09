package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convenience repository implementation to be inherited from if applicable.
 *
 * <p>Tries best to translate paging and sorting params into valid SQL.<br>
 * If result does not fit your use case: implement it yourself and do not use these convenience
 * methods.
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
          if (allowedOrderByFields != null
              && Arrays.asList(allowedOrderByFields).contains(sortField)) {
            String fullQualifiedColumnName = getColumnName(sortField);
            Direction direction = order.getDirection();
            if (direction != null && direction.isDescending()) {
              sortDirection = "DESC";
            } else {
              sortDirection = "ASC";
            }
            query
                .append(" ")
                .append("ORDER BY")
                .append(" ")
                .append(fullQualifiedColumnName)
                .append(" ")
                .append(sortDirection);
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
   * @return model properties names that are applicable for sorting, will be mapped to database
   *     column names using @see #getColumnName
   */
  protected abstract String[] getAllowedOrderByFields();

  /**
   * @param modelProperty name of model property passed as String, e.g. "lastModified"
   * @return full qualified column name as used in sql queries ("last_modified" or e.g.
   *     "w.last_modified" if prefix used in queries)
   */
  protected abstract String getColumnName(String modelProperty);

  protected String getFilterClauses(Filtering filtering) {
    if (filtering == null || filtering.getFilterCriterias().isEmpty()) {
      return "";
    }
    List<String> filterClauses =
        filtering.getFilterCriterias().stream()
            .map(this::getWhereClause)
            .collect(Collectors.toList());
    return String.join(" AND ", filterClauses);
  }

  protected String getWhereClause(FilterCriterion<?> fc)
      throws IllegalArgumentException, UnsupportedOperationException {
    StringBuilder query = new StringBuilder();
    if (fc != null) {
      FilterOperation filterOperation = fc.getOperation();
      // @see https://www.postgresql.org/docs/11/functions.html
      switch (filterOperation) {
        case BETWEEN:
          if (fc.getMinValue() == null || fc.getMaxValue() == null) {
            throw new IllegalArgumentException("For 'BETWEEN' operation two values are expected");
          } else {
            // example: BETWEEN '2015-01-01' AND '2015-12-31'
            query
                .append("(")
                .append(getColumnName(fc.getFieldName()))
                .append(" BETWEEN ")
                .append(convertToSqlString(fc.getMinValue()))
                .append(" AND ")
                .append(convertToSqlString(fc.getMaxValue()))
                .append(")");
          }
          break;
        case IN:
        case NOT_IN:
          // For 'in' or 'nin' operation
          query.append("(").append(getColumnName(fc.getFieldName()));
          if (filterOperation == FilterOperation.NOT_IN) {
            query.append(" NOT");
          }
          query.append(" IN (");
          query.append(
              fc.getValues().stream()
                  .map(this::convertToSqlString)
                  .collect(Collectors.joining(",")));
          query.append("))");
          break;
        case CONTAINS:
          // @see https://www.postgresql.org/docs/11/functions-matching.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" ILIKE '%")
              .append(convertToSqlString(fc.getValue()))
              .append("%')");
          break;
        case EQUALS:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" = ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case NOT_EQUALS:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" != ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case GREATER_THAN:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" > ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case GREATER_THAN_OR_NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" > ")
              .append(convertToSqlString(fc.getValue()))
              .append(" OR ")
              .append(getColumnName(fc.getFieldName()))
              .append(" IS NULL")
              .append(")");
          break;
        case GREATER_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" >= ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case LESS_THAN:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" < ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case LESS_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" <= ")
              .append(convertToSqlString(fc.getValue()))
              .append(")");
          break;
        case LESS_THAN_OR_EQUAL_TO_OR_NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" <= ")
              .append(convertToSqlString(fc.getValue()))
              .append(" OR ")
              .append(getColumnName(fc.getFieldName()))
              .append(" IS NULL")
              .append(")");
          break;
        case SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(getColumnName(fc.getFieldName()))
              .append(" IS NOT NULL")
              .append(")");
          break;
        case NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query.append("(").append(getColumnName(fc.getFieldName())).append(" IS NULL").append(")");
          break;
        default:
          throw new UnsupportedOperationException(filterOperation + " not supported yet");
      }
    }
    return query.toString();
  }

  protected String convertToSqlString(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof LocalDate) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      return "'" + ((ChronoLocalDate) value).format(formatter) + "'";
    }
    return value.toString();
  }
}
