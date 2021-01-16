package de.digitalcollections.cudami.server.backend.impl.database;

import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * Convenience repository implementation to be inherited from if applicable.
 *
 * <p>
 * Tries best to translate paging and sorting params into valid SQL.<br>
 * If result does not fit your use case: implement it yourself and do not use these convenience methods.
 */
public abstract class AbstractPagingAndSortingRepositoryImpl {

  public void addFiltering(Filtering filtering, StringBuilder innerQuery) {
    if (filtering != null) {
      // handle optional filtering params
      String filterClauses = getFilterClauses(filtering);
      if (!filterClauses.isEmpty()) {
        String innerQueryStr = innerQuery.toString();
        if (innerQueryStr.toUpperCase().contains(" WHERE ")) {
          innerQuery.append(" AND ");
        } else {
          innerQuery.append(" WHERE ");
        }
        innerQuery.append(filterClauses);
      }
    }
  }

  public void addFiltering(PageRequest pageRequest, StringBuilder innerQuery) {
    if (pageRequest != null) {
      addFiltering(pageRequest.getFiltering(), innerQuery);
    }
  }

  public void addLimit(PageRequest pageRequest, StringBuilder query) {
    if (pageRequest != null) {
      int pageSize = pageRequest.getPageSize();
      if (pageSize > 0) {
        query.append(" ").append("LIMIT").append(" ").append(pageSize);
      }
    }
  }

  public void addOffset(PageRequest pageRequest, StringBuilder query) {
    if (pageRequest != null) {
      int offset = pageRequest.getOffset();
      if (offset >= 0) {
        query.append(" ").append("OFFSET").append(" ").append(offset);
      }
    }
  }

  public void addOrderBy(PageRequest pageRequest, StringBuilder query) {
    if (pageRequest != null) {
      String[] allowedOrderByFields = getAllowedOrderByFields();

      // Sorting
      Sorting sorting = pageRequest.getSorting();
      if (sorting != null) {
        String orderBy
                = Optional.ofNullable(sorting.getOrders()).orElse(Collections.emptyList()).stream()
                        .filter(
                                o -> {
                                  String sortField = o.getProperty();
                                  return sortField != null
                                  && allowedOrderByFields != null
                                  && Arrays.asList(allowedOrderByFields).contains(sortField);
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
                                          "%s->>'%s' %s",
                                          fullQualifiedColumnName, subSortField.get(), sortDirection);
                                })
                        .collect(Collectors.joining(","));

        if (StringUtils.hasText(orderBy)) {
          query.append(" ORDER BY ").append(orderBy);
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

  protected String convertToSqlString(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof LocalDate) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      return "'" + ((ChronoLocalDate) value).format(formatter) + "'";
    }
    if (value instanceof String) {
      return "'" + value + "'";
    }
    return value.toString();
  }

  /**
   * @return model properties names that are applicable for sorting, will be mapped to database column names using @see #getColumnName
   */
  protected abstract String[] getAllowedOrderByFields();

  /**
   * @param modelProperty name of model property passed as String, e.g. "lastModified"
   * @return full qualified column name as used in sql queries ("last_modified" or e.g. "w.last_modified" if prefix used in queries)
   */
  protected abstract String getColumnName(String modelProperty);

  protected String getFilterClauses(Filtering filtering) {
    if (filtering == null || filtering.getFilterCriteria().isEmpty()) {
      return "";
    }
    String filterClauses
            = filtering.getFilterCriteria().stream()
                    .map(this::getWhereClause)
                    .collect(Collectors.joining(" AND "));
    return filterClauses;
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
        case LESS_THAN_AND_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
                  .append("(")
                  .append(getColumnName(fc.getFieldName()))
                  .append(" < ")
                  .append(convertToSqlString(fc.getValue()))
                  .append(" AND ")
                  .append(getColumnName(fc.getFieldName()))
                  .append(" IS NOT NULL")
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
        case LESS_THAN_OR_EQUAL_TO_AND_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
                  .append("(")
                  .append(getColumnName(fc.getFieldName()))
                  .append(" <= ")
                  .append(convertToSqlString(fc.getValue()))
                  .append(" AND ")
                  .append(getColumnName(fc.getFieldName()))
                  .append(" IS NOT NULL")
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
}
