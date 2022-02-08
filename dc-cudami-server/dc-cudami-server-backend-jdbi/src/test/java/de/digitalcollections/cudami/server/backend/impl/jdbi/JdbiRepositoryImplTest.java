package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JdbiRepositoryImplTest {

  JdbiRepositoryImpl instance = new MyImpl();

  @Test
  public void testGetWhereClauseBetween() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // BETWEEN
    String filteringProperty = "publicationStart";
    final LocalDate minDate = LocalDate.of(2020, Month.JANUARY, 1);
    final LocalDate maxDate = LocalDate.of(2020, Month.JANUARY, 31);
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).between(minDate, maxDate).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 42);

    assertEquals(
        "(publicationStart BETWEEN :filtervalue_42_min AND :filtervalue_42_max)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_42_min"), minDate);
    assertEquals(argumentMappings.get("filtervalue_42_max"), maxDate);
  }

  @Test
  public void testGetWhereClauseContains() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // NOT_IN
    String filteringProperty = "label";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).contains("Schiff").build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(label ILIKE '%' || :filtervalue_1 || '%')", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "Schiff");
  }

  @Test
  public void testGetWhereClauseEquals() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // EQUALS
    String filteringProperty = "age";
    Filtering filtering = Filtering.defaultBuilder().filter(filteringProperty).isEquals(73).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age = :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 73);
  }

  @Test
  public void testGetWhereClauseGreaterThan() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // GREATER_THAN
    String filteringProperty = "age";
    Filtering filtering = Filtering.defaultBuilder().filter(filteringProperty).greater(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age > :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseGreaterThanOrEqualTo() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // GREATER_THAN_OR_EQUAL_TO
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).greaterOrEqual(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age >= :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseGreaterThanOrNotSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // GREATER_THAN_OR_NOT_SET
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).greaterOrNotSet(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age > :filtervalue_1 OR age IS NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseLessThan() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // LESS_THAN
    String filteringProperty = "age";
    Filtering filtering = Filtering.defaultBuilder().filter(filteringProperty).less(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age < :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseLessThanAndSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // LESS_THAN_AND_SET
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).lessAndSet(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age < :filtervalue_1 AND age IS NOT NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseLessThanOrEqualTo() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // LESS_THAN_OR_EQUAL_TO
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).lessOrEqual(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age <= :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseLessThanOrEqualToAndSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // LESS_THAN_OR_EQUAL_TO_AND_SET
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).lessOrEqualAndSet(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age <= :filtervalue_1 AND age IS NOT NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseLessThanOrEqualToOrNotSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // LESS_THAN_OR_EQUAL_TO_OR_NOT_SET
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).lessOrEqualOrNotSet(18).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age <= :filtervalue_1 OR age IS NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 18);
  }

  @Test
  public void testGetWhereClauseNotEquals() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // NOT_EQUALS
    String filteringProperty = "label";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).notEquals("undefined").build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(label != :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "undefined");
  }

  @Test
  public void testGetWhereClauseNotIn() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // NOT_IN
    String filteringProperty = "name";
    List<String> values = Arrays.asList("Hans", "Sepp", "Max", "Moritz");
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).notIn(values).build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals(
        "(name NOT IN (:filtervalue_1_1,:filtervalue_1_2,:filtervalue_1_3,:filtervalue_1_4))",
        whereClause);
    assertEquals(argumentMappings.get("filtervalue_1_1"), "Hans");
    assertEquals(argumentMappings.get("filtervalue_1_2"), "Sepp");
    assertEquals(argumentMappings.get("filtervalue_1_3"), "Max");
    assertEquals(argumentMappings.get("filtervalue_1_4"), "Moritz");
  }

  @Test
  public void testGetWhereClauseNotSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // NOT_SET
    String filteringProperty = "lastModified";
    Filtering filtering = Filtering.defaultBuilder().filter(filteringProperty).notSet().build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(lastModified IS NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), null);
  }

  @Test
  public void testGetWhereClauseSet() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // SET
    String filteringProperty = "lastModified";
    Filtering filtering = Filtering.defaultBuilder().filter(filteringProperty).set().build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(lastModified IS NOT NULL)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), null);
  }

  @Test
  public void testGetWhereClauseStartsWith() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // STARTS_WITH
    String filteringProperty = "label";
    Filtering filtering =
        Filtering.defaultBuilder().filter(filteringProperty).startsWith("Donau").build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(label ILIKE :filtervalue_1 || '%')", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "Donau");
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

    @Override
    protected String getUniqueField() {
      return "uuid";
    }
  }
}
