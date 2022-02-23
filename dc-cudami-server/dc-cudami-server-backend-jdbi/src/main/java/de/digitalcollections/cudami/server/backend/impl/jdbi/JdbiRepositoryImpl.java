package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.impl.database.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.paging.PageRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class JdbiRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbiRepositoryImpl.class);
  private static final String KEY_PREFIX_FILTERVALUE = "filtervalue_";

  private static Pattern selectStmtSplitter;

  protected final Jdbi dbi;
  protected final String mappingPrefix;
  protected final String tableAlias;
  protected final String tableName;
  protected int offsetForAlternativePaging;

  public JdbiRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      int offsetForAlternativePaging) {
    this.dbi = dbi;
    this.mappingPrefix = mappingPrefix;
    this.tableName = tableName;
    this.tableAlias = tableAlias;
    this.offsetForAlternativePaging = offsetForAlternativePaging;
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
        String sqlQueryStr = sqlQuery.toString();
        if (sqlQueryStr.toUpperCase().contains(" WHERE ")) {
          sqlQuery.append(" AND ");
        } else {
          sqlQuery.append(" WHERE ");
        }
        sqlQuery.append(filterClauses);
      }
    }
  }

  @Override
  protected void addPageRequestParams(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest.getOffset() < offsetForAlternativePaging) {
      super.addPageRequestParams(pageRequest, sqlQuery);
    } else {
      buildPageRequestSql(pageRequest, sqlQuery);
    }
  }

  /**
   * For examples showing what should happen here see JdbiRepositoryImplTest#testAlternativePaging
   * and following ones.
   */
  // Spotbugs complains about l. 94 - ignore it
  @SuppressFBWarnings("LI_LAZY_INIT_STATIC")
  protected void buildPageRequestSql(PageRequest pageRequest, StringBuilder innerSql) {
    if (pageRequest == null || innerSql == null) {
      return;
    }
    if (JdbiRepositoryImpl.selectStmtSplitter == null) {
      // init this field here where we use it to keep it in sight
      JdbiRepositoryImpl.selectStmtSplitter =
          Pattern.compile(
              "(?iu)^\\s*(SELECT\\s+(?<fields>.+)\\s+)?FROM\\s+(?<fromwhere>(?<table>\\w+\\b)(\\s+AS\\s+(?<tablealias>\\w+))?.*?(\\sWHERE.+?)?)(\\sORDER BY\\s+(?<orderings>.+))?\\s*$");
    }
    Matcher selectStmtMatcher = selectStmtSplitter.matcher(innerSql.toString());
    if (!selectStmtMatcher.find()) {
      JdbiRepositoryImpl.LOGGER.warn(
          "Regex 'selectStmtSplitter' did not match on << {} >>", innerSql.toString());
      super.addPageRequestParams(pageRequest, innerSql);
      return;
    }
    String fields = selectStmtMatcher.group("fields");
    String fromWherePart = selectStmtMatcher.group("fromwhere");
    String table = selectStmtMatcher.group("table").strip();
    String tableAlias = selectStmtMatcher.group("tablealias");
    String orderings = selectStmtMatcher.group("orderings");
    String pageRequestOrderings = getOrderBy(pageRequest.getSorting());
    if (!StringUtils.hasText(orderings) && StringUtils.hasText(pageRequestOrderings)) {
      orderings = pageRequestOrderings.strip();
    } else if (StringUtils.hasText(orderings) && StringUtils.hasText(pageRequestOrderings)) {
      orderings = String.format("%s, %s", orderings.strip(), pageRequestOrderings.strip());
    }
    int offset = pageRequest.getOffset();
    int pageSize = pageRequest.getPageSize();
    // clear passed StringBuilder and rebuild the select statement
    innerSql.delete(0, innerSql.length());
    // outer SELECT part
    innerSql.append("SELECT * FROM (");
    // inner SELECT part
    if (fields != null && fields.contains("*")) {
      fields =
          fields.replaceFirst(
              "(\\w+[.])?[*]",
              String.format(
                  "%s.%s rnsetid", tableAlias != null ? tableAlias : table, getUniqueField()));
    } else if (fields == null) {
      fields =
          String.format("%s.%s rnsetid", tableAlias != null ? tableAlias : table, getUniqueField());
    }
    innerSql
        .append("SELECT row_number() OVER (")
        .append(
            StringUtils.hasText(orderings) ? String.format("ORDER BY %s", orderings.strip()) : "")
        .append(") rn, ")
        .append(fields);
    // inner FROM and WHERE parts
    innerSql.append(String.format(" FROM %s", fromWherePart));
    // outer SELECT statement goes on: FROM part
    innerSql.append(") innerselect_rownumber ");
    if (fields.contains("rnsetid")) {
      // add a join
      innerSql
          .append("INNER JOIN ")
          .append(table)
          .append(
              String.format(" ON %s.%s = innerselect_rownumber.rnsetid ", table, getUniqueField()));
    }
    // last but not least: outer WHERE part
    // we are using a range here since its costs are lower than of a BETWEEN or two comparisons
    // see also https://www.postgresql.org/docs/12/rangetypes.html,
    // https://www.postgresql.org/docs/12/functions-range.html
    // it is simply written as a mathematics intervall
    innerSql.append(
        String.format(
            "WHERE '(%d,%d]'::int8range @> innerselect_rownumber.rn", offset, offset + pageSize));
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
      String givenExpression = fc.getExpression();
      String expression;
      if (fc.isNativeExpression()) {
        // safe (not created using user input)
        expression = givenExpression;
      } else {
        // may be created using user input: map expression to column name
        expression = getColumnName(givenExpression);
        if (expression == null) {
          throw new IllegalArgumentException(
              String.format("Given expression '%s' is invalid / can not be mapped.")
                  + givenExpression);
        }
      }

      String criterionKey = KEY_PREFIX_FILTERVALUE + criterionCount;
      switch (filterOperation) {
          // @see https://www.postgresql.org/docs/11/functions.html
        case BETWEEN:
          if (fc.getMinValue() == null || fc.getMaxValue() == null) {
            throw new IllegalArgumentException("For 'BETWEEN' operation two values are expected");
          } else {
            // example: BETWEEN '2015-01-01' AND '2015-12-31'
            String keyMin = criterionKey + "_min";
            String keyMax = criterionKey + "_max";
            query
                .append("(")
                .append(expression)
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
          query.append("(").append(expression);
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
              .append(expression)
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
              .append(expression)
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
              .append(expression)
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
              .append(expression)
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
              .append(expression)
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
              .append(expression)
              .append(" > ")
              .append(":")
              .append(criterionKey)
              .append(" OR ")
              .append(expression)
              .append(" IS NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case GREATER_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(expression)
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
              .append(expression)
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
              .append(expression)
              .append(" < ")
              .append(":")
              .append(criterionKey)
              .append(" AND ")
              .append(expression)
              .append(" IS NOT NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_OR_EQUAL_TO:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(expression)
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
              .append(expression)
              .append(" <= ")
              .append(":")
              .append(criterionKey)
              .append(" AND ")
              .append(expression)
              .append(" IS NOT NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case LESS_THAN_OR_EQUAL_TO_OR_NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query
              .append("(")
              .append(expression)
              .append(" <= ")
              .append(":")
              .append(criterionKey)
              .append(" OR ")
              .append(expression)
              .append(" IS NULL")
              .append(")");
          argumentMappings.put(criterionKey, fc.getValue());
          break;
        case SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query.append("(").append(expression).append(" IS NOT NULL").append(")");
          break;
        case NOT_SET:
          // @see https://www.postgresql.org/docs/11/functions-comparison.html
          query.append("(").append(expression).append(" IS NULL").append(")");
          break;
        default:
          throw new UnsupportedOperationException(filterOperation + " not supported yet");
      }
    }
    return query.toString();
  }

  protected Integer retrieveNextSortIndexForParentChildren(
      Jdbi dbi, String tableName, String columNameParentUuid, UUID parentUuid) {
    // first child: max gets no results (= null)):
    Integer sortIndex =
        dbi.withHandle(
            (Handle h) ->
                h.createQuery(
                        "SELECT MAX(sortIndex) + 1 FROM "
                            + tableName
                            + " WHERE "
                            + columNameParentUuid
                            + " = :parent_uuid")
                    .bind("parent_uuid", parentUuid)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(null));
    if (sortIndex == null) {
      return 0;
    }
    return sortIndex;
  }
}
