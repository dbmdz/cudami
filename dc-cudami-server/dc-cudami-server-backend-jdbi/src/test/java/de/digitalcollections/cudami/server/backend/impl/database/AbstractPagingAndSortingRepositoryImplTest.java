package de.digitalcollections.cudami.server.backend.impl.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.Sorting;
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
    PageRequest pr = new PageRequest();
    repository.addOrderBy(pr, query);
    assertEquals("", query.toString().trim());

    Sorting sorting = new Sorting();
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals("", query.toString().trim());

    Order order = new Order();
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals("", query.toString().trim());

    order = new Order("ham");
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals("", query.toString().trim());

    order = new Order("foo");
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    repository.addOrderBy(pr, query);
    assertEquals("ORDER BY foo ASC", query.toString().trim());

    order = new Order(Direction.DESC, "foo");
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals("ORDER BY foo DESC", query.toString().trim());

    order = new Order("foo");
    order.setSubProperty("bar");
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals("ORDER BY foo->>'bar' ASC", query.toString().trim());

    order = new Order(Direction.DESC, "foo");
    order.setSubProperty("bar");
    sorting = new Sorting(order);
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals("ORDER BY foo->>'bar' DESC", query.toString().trim());

    Order secondOrder = new Order("foo");
    sorting = sorting.and(new Sorting(secondOrder));
    pr.setSorting(sorting);
    query = new StringBuilder("");
    repository.addOrderBy(pr, query);
    assertEquals("ORDER BY foo->>'bar' DESC,foo ASC", query.toString().trim());
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

    @Override
    protected String getUniqueField() {
      return null;
    }
  }
}
