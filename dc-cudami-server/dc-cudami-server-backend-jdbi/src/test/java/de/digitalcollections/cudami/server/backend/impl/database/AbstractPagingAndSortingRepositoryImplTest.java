package de.digitalcollections.cudami.server.backend.impl.database;

import static org.junit.jupiter.api.Assertions.*;

import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AbstractPagingAndSortingRepositoryImplTest {

  public AbstractPagingAndSortingRepositoryImplTest() {}

  @Test
  public void testAddOrderBy() {
    PagingAndSortingRepositoryImpl repository = new PagingAndSortingRepositoryImpl();
    repository.setAllowedOrderByFields(Arrays.asList("foo", "bar"));
    repository.setColumnName("foo");

    StringBuilder query = new StringBuilder("");
    PageRequest pr = new PageRequestImpl();
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "");

    Sorting sorting = new SortingImpl();
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "");

    OrderImpl order = new OrderImpl();
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "");

    order = new OrderImpl("ham");
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "");

    order = new OrderImpl("foo");
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "ORDER BY foo ASC");

    order = new OrderImpl(Direction.DESC, "foo");
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "ORDER BY foo DESC");

    order = new OrderImpl("foo");
    order.setSubProperty("bar");
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "ORDER BY foo->>'bar' ASC");

    order = new OrderImpl(Direction.DESC, "foo");
    order.setSubProperty("bar");
    sorting = new SortingImpl(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "ORDER BY foo->>'bar' DESC");

    OrderImpl secondOrder = new OrderImpl("foo");
    sorting = sorting.and(new SortingImpl(secondOrder));
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals(query.toString().trim(), "ORDER BY foo->>'bar' DESC,foo ASC");
  }

  @Test
  public void testFiltering() {
    String filteringProperty = "publicationStart";
    PagingAndSortingRepositoryImpl repository = new PagingAndSortingRepositoryImpl();
    repository.setColumnName(filteringProperty);

    Filtering filtering =
        Filtering.defaultBuilder()
            .filter(filteringProperty)
            .between(LocalDate.of(2020, Month.JANUARY, 1), LocalDate.of(2020, Month.JANUARY, 31))
            .build();
    FilterCriterion<?> filterCriterion = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = repository.getWhereClause(filterCriterion);
    assertEquals(
        String.format("(%s BETWEEN '2020-01-01' AND '2020-01-31')", filteringProperty),
        whereClause);
  }

  private class PagingAndSortingRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl {
    List<String> allowedOrderByFields = null;
    String columnName = null;

    @Override
    protected List<String> getAllowedOrderByFields() {
      return allowedOrderByFields;
    }

    @Override
    protected String getColumnName(String modelProperty) {
      return columnName;
    }

    protected void setAllowedOrderByFields(List<String> fields) {
      allowedOrderByFields = fields;
    }

    protected void setColumnName(String name) {
      columnName = name;
    }
  }
}
