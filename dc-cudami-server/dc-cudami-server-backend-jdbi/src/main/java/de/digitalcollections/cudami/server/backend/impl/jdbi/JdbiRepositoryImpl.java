package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.impl.database.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.paging.PageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;

public abstract class JdbiRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl {

  private static final String KEY_PREFIX_FILTERVALUE = "filtervalue_";

  protected final Jdbi dbi;
  protected final String mappingPrefix;
  protected final String tableAlias;
  protected final String tableName;

  public JdbiRepositoryImpl(Jdbi dbi, String tableName, String tableAlias, String mappingPrefix) {
    this.dbi = dbi;
    this.mappingPrefix = mappingPrefix;
    this.tableName = tableName;
    this.tableAlias = tableAlias;
  }

  public void addFiltering(
      PageRequest pageRequest, StringBuilder sqlQuery, Map<String, Object> argumentMappings) {
    if (pageRequest != null) {
      addFiltering(pageRequest.getFiltering(), sqlQuery, argumentMappings);
    }
  }

  public void addFiltering(
      Filtering filtering, StringBuilder sqlQuery, Map<String, Object> argumentMappings) {
    if (filtering != null) {
      // handle optional filtering params
      String filterClauses = getFilterClauses(filtering, argumentMappings);
      if (!filterClauses.isEmpty()) {
        String innerQueryStr = sqlQuery.toString();
        if (innerQueryStr.toUpperCase().contains(" WHERE ")) {
          sqlQuery.append(" AND ");
        } else {
          sqlQuery.append(" WHERE ");
        }
        sqlQuery.append(filterClauses);
      }
    }
  }

  public long count() {
    final String sql = "SELECT count(*) FROM " + tableName;
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  protected String getFilterClauses(Filtering filtering, Map<String, Object> argumentMappings) {
    if (filtering == null || filtering.getFilterCriteria().isEmpty()) {
      return "";
    }

    ArrayList<String> whereClauses = new ArrayList<>();
    List<FilterCriterion> filterCriteria = filtering.getFilterCriteria();
    int criterionCount = argumentMappings.size() + 1;
    for (FilterCriterion filterCriterion : filterCriteria) {
      String whereClause = getWhereClause(filterCriterion, argumentMappings, criterionCount);
      whereClauses.add(whereClause);
      criterionCount++;
    }

    String filterClauses = whereClauses.stream().collect(Collectors.joining(" AND "));
    return filterClauses;
  }

  public String getMappingPrefix() {
    return mappingPrefix;
  }

  public String getTableAlias() {
    return tableAlias;
  }

  public String getTableName() {
    return tableName;
  }

  protected String getWhereClause(
      FilterCriterion<?> fc, Map<String, Object> argumentMappings, int criterionCount)
      throws IllegalArgumentException, UnsupportedOperationException {
    StringBuilder query = new StringBuilder();
    if (fc != null) {
      FilterOperation filterOperation = fc.getOperation();
      // @see https://www.postgresql.org/docs/11/functions.html
      final String fieldName = fc.getFieldName();
      final String columnName = getColumnName(fieldName);
      final String leftSide = (columnName != null) ? columnName : fieldName;
      // basic sql injection detection:
      if (leftSide.contains("'") || leftSide.contains(";")) {
        throw new IllegalArgumentException(
            String.format("leftSide '%s' seems to contain malicious code!", leftSide));
      }

      String criterionKey = KEY_PREFIX_FILTERVALUE + criterionCount;
      switch (filterOperation) {
        case BETWEEN:
          if (fc.getMinValue() == null || fc.getMaxValue() == null) {
            throw new IllegalArgumentException("For 'BETWEEN' operation two values are expected");
          } else {
            // example: BETWEEN '2015-01-01' AND '2015-12-31'
            String keyMin = criterionKey + "_min";
            String keyMax = criterionKey + "_max";
            query
                .append("(")
                .append(leftSide)
                .append(" BETWEEN ")
                .append(":")
                .append(keyMin)
                .append(" AND ")
                .append(":")
                .append(keyMax)
                .append(")");
            argumentMappings.put(keyMin, fc.getMinValue());
            argumentMappings.put(keyMax, fc.getMaxValue());
          }
          break;
        case IN:
        case NOT_IN:
          if (fc.getValues() == null || fc.getValues().isEmpty()) {
            throw new IllegalArgumentException(
                "For 'IN/NOT_IN' operation at least one value is expected");
          }
          // For 'in' or 'nin' operation
          query.append("(").append(leftSide);
          if (filterOperation == FilterOperation.NOT_IN) {
            query.append(" NOT");
          }
          query.append(" IN (");

          ArrayList<String> values = new ArrayList<>();
          AtomicInteger valueCounter = new AtomicInteger(0);
          fc.getValues()
              .forEach(
                  v -> {
                    String key = criterionKey + "_" + valueCounter.incrementAndGet();
                    values.add(":" + key);
                    argumentMappings.put(key, v);
                  });
          query.append(values.stream().collect(Collectors.joining(",")));

          query.append("))");
          break;
        case CONTAINS:
          // @see https://www.postgresql.org/docs/11/functions-matching.html
          query
              .append("(")
              .append(leftSide)
              .append(" ILIKE '%' || ")
              .append(":")
              .append(criterionKey)
              .append(" || '%')");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case STARTS_WITH:
          // @see https://www.postgresql.org/docs/11/functions-matching.html
          query
              .append("(")
              .append(leftSide)
              .append(" ILIKE ")
              .append(":")
              .append(criterionKey)
              .append(" || '%')");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case EQUALS:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" = ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case NOT_EQUALS:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" != ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case GREATER_THAN:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" > ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case GREATER_THAN_OR_NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" > ")
              .append(":")
              .append(criterionKey)
              .append(" OR ")
              .append(leftSide)
              .append(" IS NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case GREATER_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" >= ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" < ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_AND_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" < ")
              .append(":")
              .append(criterionKey)
              .append(" AND ")
              .append(leftSide)
              .append(" IS NOT NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" <= ")
              .append(":")
              .append(criterionKey)
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_OR_EQUAL_TO_AND_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" <= ")
              .append(":")
              .append(criterionKey)
              .append(" AND ")
              .append(leftSide)
              .append(" IS NOT NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_OR_EQUAL_TO_OR_NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(leftSide)
              .append(" <= ")
              .append(":")
              .append(criterionKey)
              .append(" OR ")
              .append(leftSide)
              .append(" IS NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query.append("(").append(leftSide).append(" IS NOT NULL").append(")");
          break;
        case NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query.append("(").append(leftSide).append(" IS NULL").append(")");
          break;
        default:
          throw new UnsupportedOperationException(filterOperation + " not supported yet");
      }
    }
    return query.toString();
  }
}
