package de.digitalcollections.cudami.server.backend.impl.database;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private static Pattern SELECT_STMT_SPLITTER;
  protected int offsetForAlternativePaging;

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
        } else {
          sqlQuery.append(", ");
        }
        sqlQuery.append(orderBy);
      }
    }
  }

  /*
   * @param pageRequest
   * @param sqlQuery
   */
  public void addPageRequestParams(PageRequest pageRequest, StringBuilder sqlQuery) {
    if (pageRequest != null) {
      if (pageRequest.getOffset() < offsetForAlternativePaging) {
        // standard handling with LIMIT and OFFSET
        buildPageRequestSqlUsingOffsetAndLimit(pageRequest, sqlQuery);
      } else {
        // special handling with window function (row_number())
        buildPageRequestSqlUsingWindowFunction(pageRequest, sqlQuery);
      }
    }
  }

  private void buildPageRequestSqlUsingOffsetAndLimit(
      PageRequest pageRequest, StringBuilder sqlQuery) {
    addOrderBy(pageRequest, sqlQuery);
    addLimit(pageRequest, sqlQuery);
    addOffset(pageRequest, sqlQuery);
  }

  /**
   * For examples showing what should happen here see JdbiRepositoryImplTest#testAlternativePaging
   * and following ones.
   */
  @SuppressFBWarnings(
      value = "LI_LAZY_INIT_STATIC",
      justification = "Spotbugs complains about l. 95 - ignore it")
  private void buildPageRequestSqlUsingWindowFunction(
      PageRequest pageRequest, StringBuilder innerSql) {
    if (pageRequest == null || innerSql == null) {
      return;
    }
    if (SELECT_STMT_SPLITTER == null) {
      // init this field here where we use it to keep it in sight
      // the flags at the start stand for:
      // - i: case insensitive
      // - u: unicode aware case folding
      SELECT_STMT_SPLITTER =
          Pattern.compile(
              "(?iu)^\\s*(SELECT\\s+(?<fields>.+)\\s+)?FROM\\s+(?<fromwhere>(?<table>\\w+\\b)(\\s+AS\\s+(?<tablealias>\\w+))?.*?(\\sWHERE.+?)?)(\\sORDER BY\\s+(?<orderings>.+))?\\s*$");
    }
    Matcher selectStmtMatcher = SELECT_STMT_SPLITTER.matcher(innerSql.toString());
    if (!selectStmtMatcher.find()) {
      LOGGER.warn("Regex 'selectStmtSplitter' did not match on << {} >>", innerSql.toString());
      // fallback to limit-offset
      buildPageRequestSqlUsingOffsetAndLimit(pageRequest, innerSql);
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
  public abstract String getColumnName(String modelProperty);

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
                  String formatString;
                  if (subSortField.isEmpty()) {
                    if (supportsCaseSensitivityForProperty(sortField) && o.isIgnoreCase()) {
                      // FIXME: exception at showing/sorting empty person list:
                      /*
                                             * Got 500 for backend call GET /v6/persons?pageNumber=0&pageSize=10&sortBy=label.asc.ignorecase.
                      ⤷ http://localhost:9000/v6/persons?pageNumber=0&pageSize=10&sortBy=label.asc.ignorecase
                                             * ERROR: function lower(jsonb) does not exist; Hinweis: No function matches the given name and argument types. You might need to add explicit type casts.
                                             */
                      formatString = "lower(%s) %s";
                    } else {
                      // case sensitive or not supported at all
                      formatString = "%s %s";
                    }
                    return String.format(formatString, fullQualifiedColumnName, sortDirection);
                  }
                  // `->>` always returns varchar so we obey case sensitivity
                  // The COALESCE function returns the first of its arguments that is not null. Null
                  // is returned
                  // only if all arguments are null. (from Postgres doc)
                  if (o.isIgnoreCase()) {
                    formatString =
                        "lower(COALESCE(%1$s->>'%2$s', %1$s->>'')) COLLATE \"ucs_basic\" %3$s";
                  } else {
                    formatString = "COALESCE(%1$s->>'%2$s', %1$s->>'') COLLATE \"ucs_basic\" %3$s";
                  }
                  return String.format(
                      formatString, fullQualifiedColumnName, subSortField.get(), sortDirection);
                })
            .collect(Collectors.joining(", "));
    return orderBy;
  }

  /**
   * @return name of model property that guarantees an unique sorting, e.g. a db primary key or
   *     another unique column/field
   */
  protected abstract String getUniqueField();

  /**
   * Returns whether the passed property can be sorted case sensitive (in general). This method
   * should be kept in sync with {@link #getAllowedOrderByFields()}.
   *
   * @param modelProperty the property name (similar to {@link #getColumnName})
   * @return {@code true} only if case (in)sensitive sorting is possible
   */
  protected abstract boolean supportsCaseSensitivityForProperty(String modelProperty);
}
