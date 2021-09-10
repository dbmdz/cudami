package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JdbiRepositoryImplTest {

  JdbiRepositoryImpl instance = new MyImpl();

  @Test
  public void testGetWhereClause() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // BETWEEN
    String filteringProperty = "publicationStart";
    final LocalDate minDate = LocalDate.of(2020, Month.JANUARY, 1);
    final LocalDate maxDate = LocalDate.of(2020, Month.JANUARY, 31);
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).between(minDate, maxDate).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 17);

    assertEquals(
        String.format(
            "(%s BETWEEN :filtervalue_17_min AND :filtervalue_17_max)", filteringProperty),
        whereClause);
    assertEquals(argumentMappings.get("filtervalue_17_min"), minDate);
    assertEquals(argumentMappings.get("filtervalue_17_max"), maxDate);
  }

  private class MyImpl extends JdbiRepositoryImpl {

    public MyImpl() {
      super(null, null, null, null);
    }

    @Override
    protected List<String> getAllowedOrderByFields() {
      throw new UnsupportedOperationException(
          "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getColumnName(String modelProperty) {
      return modelProperty;
    }
  }
}
