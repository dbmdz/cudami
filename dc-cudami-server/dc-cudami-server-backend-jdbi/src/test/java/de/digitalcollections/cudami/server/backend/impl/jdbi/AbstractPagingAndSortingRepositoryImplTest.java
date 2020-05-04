package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.junit.jupiter.api.Assertions.*;

import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

public class AbstractPagingAndSortingRepositoryImplTest {

  public AbstractPagingAndSortingRepositoryImplTest() {}

  @Test
  public void testAddOrderBy() {
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("publicationStart")
            .between(LocalDate.of(2020, Month.JANUARY, 1), LocalDate.of(2020, Month.JANUARY, 31))
            .build();
    FilterCriterion<?> filterCriterion = filtering.getFilterCriterionFor("publicationStart");
    AbstractPagingAndSortingRepositoryImpl instance =
        new AbstractPagingAndSortingRepositoryImplImpl();
    String whereClause = instance.getWhereClause(filterCriterion);
    assertEquals("( BETWEEN '2020-01-01' AND '2020-01-31')", whereClause);
  }

  public class AbstractPagingAndSortingRepositoryImplImpl
      extends AbstractPagingAndSortingRepositoryImpl {

    public String[] getAllowedOrderByFields() {
      return null;
    }

    public String getColumnName(String modelProperty) {
      return "";
    }

    protected String getWhereClause(FilterCriterion<?> fc) {
      return super.getWhereClause(fc);
    }
  }
}
