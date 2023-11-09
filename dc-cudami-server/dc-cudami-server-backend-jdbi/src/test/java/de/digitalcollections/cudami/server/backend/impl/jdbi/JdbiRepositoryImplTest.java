package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The JdbiRepository")
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .between(minDate, maxDate)
                    .build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .contains("Schiff")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(label ILIKE '%' || :filtervalue_1 || '%')", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "Schiff");
  }

  @DisplayName("can create a WhereClause for collection contains")
  @Test
  public void testGetWhereClauseContainsCollection() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // NOT_IN
    String filteringProperty = "foo_uuids";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.nativeBuilder()
                    .withExpression(filteringProperty)
                    .withNativeExpression(true)
                    .contains(List.of(uuid1, uuid2))
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(foo_uuids @> :filtervalue_1::UUID[])", whereClause);
    UUID[] actual = (UUID[]) argumentMappings.get("filtervalue_1");
    assertThat(actual).containsExactly(uuid1, uuid2);
  }

  @Test
  public void testGetWhereClauseEquals() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // EQUALS
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).isEquals(73).build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(age = :filtervalue_1)", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), 73);
  }

  @DisplayName("can create a WhereClause for collection equality")
  @Test
  public void testGetWhereClauseEqualsForCollection() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // EQUALS
    String filteringProperty = "foo_uuids";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .withNativeExpression(true)
                    .isEquals(List.of(uuid1, uuid2))
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(foo_uuids = :filtervalue_1::UUID[])", whereClause);
    UUID[] actual = (UUID[]) argumentMappings.get("filtervalue_1");
    assertThat(actual).containsExactly(uuid1, uuid2);
  }

  @Test
  public void testGetWhereClauseGreaterThan() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // GREATER_THAN
    String filteringProperty = "age";
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).greater(18).build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .greaterOrEqual(18)
                    .build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .greaterOrNotSet(18)
                    .build())
            .build();
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
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).less(18).build())
            .build();
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
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).lessAndSet(18).build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder().withExpression(filteringProperty).lessOrEqual(18).build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .lessOrEqualAndSet(18)
                    .build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .lessOrEqualOrNotSet(18)
                    .build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .notEquals("undefined")
                    .build())
            .build();
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
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).notIn(values).build())
            .build();
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
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).notSet().build())
            .build();
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
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(filteringProperty).set().build())
            .build();
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .startsWith("Donau")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("(label ILIKE :filtervalue_1 || '%')", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "Donau");
  }

  @Test
  public void testGetWhereClauseRegex() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    String filteringProperty = "label";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .regex("(some)? *label text")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("label ~ :filtervalue_1", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "(some)? *label text");
  }

  @Test
  public void testGetWhereClauseIRegex() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    String filteringProperty = "label";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .iregex("(some)? *label text")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("label ~* :filtervalue_1", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "(some)? *label text");
  }

  @Test
  public void testGetWhereClauseNotRegex() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    String filteringProperty = "label";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .notRegex("(some)? *label text")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("label !~ :filtervalue_1", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "(some)? *label text");
  }

  @Test
  public void testGetWhereClauseNotIRegex() {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    String filteringProperty = "label";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(filteringProperty)
                    .notIRegex("(some)? *label text")
                    .build())
            .build();
    FilterCriterion<?> fc = filtering.getFilterCriterionFor(filteringProperty);
    String whereClause = instance.getWhereClause(fc, argumentMappings, 1);

    assertEquals("label !~* :filtervalue_1", whereClause);
    assertEquals(argumentMappings.get("filtervalue_1"), "(some)? *label text");
  }

  @Test
  public void testDefaultPaging() {
    StringBuilder innerSql = new StringBuilder("SELECT d.* FROM digitalobjects AS d");
    PageRequest pageRequest =
        new PageRequest(
            0,
            1000,
            new Sorting(
                new Order(Direction.DESC, "last_modified"), new Order(Direction.ASC, "uuid")));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT d.* FROM digitalobjects AS d ORDER BY last_modified DESC, uuid ASC LIMIT 1000 OFFSET 0",
        innerSql.toString());
  }

  @Test
  public void testDefaultPaging2() {
    StringBuilder innerSql = new StringBuilder("SELECT * FROM digitalobjects AS d");
    PageRequest pageRequest =
        new PageRequest(
            1,
            1000,
            new Sorting(
                new Order(Direction.DESC, "last_modified"), new Order(Direction.ASC, "uuid")));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM digitalobjects AS d ORDER BY last_modified DESC, uuid ASC LIMIT 1000 OFFSET 1000",
        innerSql.toString());
  }

  @Test
  public void testAlternativePaging() {
    StringBuilder innerSql = new StringBuilder("SELECT * FROM digitalobjects AS d");
    PageRequest pageRequest =
        new PageRequest(
            5,
            1000,
            new Sorting(
                new Order(Direction.DESC, "last_modified"), new Order(Direction.ASC, "uuid")));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM (SELECT row_number() OVER (ORDER BY last_modified DESC, uuid ASC) rn, d.uuid rnsetid FROM digitalobjects AS d) innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(5000,6000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithAliasInSelect() {
    StringBuilder innerSql = new StringBuilder("SELECT d.* FROM digitalobjects AS d");
    PageRequest pageRequest =
        new PageRequest(
            5,
            1000,
            new Sorting(
                new Order(Direction.DESC, "last_modified"), new Order(Direction.ASC, "uuid")));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM (SELECT row_number() OVER (ORDER BY last_modified DESC, uuid ASC) rn, d.uuid rnsetid FROM digitalobjects AS d) innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(5000,6000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithFieldsInSelect() {
    StringBuilder innerSql =
        new StringBuilder(
            "SELECT d.sortindex AS idx, d.* FROM digitalobjects AS d "
                + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid WHERE cd.collection_uuid = :uuid");
    PageRequest pageRequest =
        new PageRequest(7, 1000, new Sorting(new Order(Direction.DESC, "last_modified")));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM ("
            + "SELECT row_number() OVER (ORDER BY last_modified DESC) rn, d.sortindex AS idx, d.uuid rnsetid FROM digitalobjects AS d "
            + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid WHERE cd.collection_uuid = :uuid"
            + ") innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(7000,8000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithFieldsInSelectAndOrderBy() {
    StringBuilder innerSql =
        new StringBuilder(
            "SELECT d.sortindex AS idx, d.* FROM digitalobjects AS d "
                + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid "
                + "WHERE cd.collection_uuid = :uuid "
                + "ORDER BY d.sortindex ASC");
    PageRequest pageRequest = new PageRequest(7, 1000);
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM ("
            + "SELECT row_number() OVER (ORDER BY d.sortindex ASC) rn, d.sortindex AS idx, d.uuid rnsetid FROM digitalobjects AS d "
            + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid WHERE cd.collection_uuid = :uuid"
            + ") innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(7000,8000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithFieldsInSelectAndOrderByAndSorting() {
    StringBuilder innerSql =
        new StringBuilder(
            "SELECT d.sortindex AS idx, * FROM digitalobjects AS d "
                + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid "
                + "WHERE cd.collection_uuid = :uuid "
                + "ORDER BY d.sortindex ASC");
    PageRequest pageRequest =
        new PageRequest(7, 1000, new Sorting(Direction.DESC, "last_modified"));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM ("
            + "SELECT row_number() OVER (ORDER BY d.sortindex ASC, last_modified DESC) rn, d.sortindex AS idx, d.uuid rnsetid FROM digitalobjects AS d "
            + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid "
            + "WHERE cd.collection_uuid = :uuid"
            + ") innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(7000,8000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithoutSorting() {
    StringBuilder innerSql =
        new StringBuilder(
            "SELECT d.sortindex AS idx, * FROM digitalobjects AS d "
                + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid WHERE cd.collection_uuid = :uuid");
    PageRequest pageRequest = new PageRequest(7, 1000);
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM ("
            + "SELECT row_number() OVER () rn, d.sortindex AS idx, d.uuid rnsetid FROM digitalobjects AS d "
            + "LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid WHERE cd.collection_uuid = :uuid"
            + ") innerselect_rownumber "
            + "INNER JOIN digitalobjects ON digitalobjects.uuid = innerselect_rownumber.rnsetid "
            + "WHERE '(7000,8000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  @Test
  public void testAlternativePagingWithFullFields() {
    StringBuilder innerSql =
        new StringBuilder(
            "SELECT ua.created ua_created, ua.last_published ua_lastPublished, ua.primary ua_primary, ua.slug ua_slug, ua.target_identifiable_type "
                + "ua_targetIdentifiableType, ua.target_identifiable_objecttype ua_targetIdentifiableObjectType, ua.target_language ua_targetLanguage, ua.target_uuid ua_targetUuid, "
                + "ua.uuid ua_uuid, ua.website_uuid ua_websiteUuid, webs.uuid webs_uuid, webs.label webs_label, webs.url webs_url "
                + "FROM url_aliases AS ua LEFT JOIN websites webs ON webs.uuid = ua.website_uuid "
                + "WHERE (ua.slug ILIKE '%' || :filtervalue_1 || '%')");
    PageRequest pageRequest = new PageRequest(7, 1000, new Sorting(Direction.ASC, "slug"));
    instance.addPagingAndSorting(pageRequest, innerSql);
    assertEquals(
        "SELECT * FROM ("
            + "SELECT row_number() OVER (ORDER BY slug ASC) rn, ua.created ua_created, ua.last_published ua_lastPublished, ua.primary ua_primary, ua.slug ua_slug, ua.target_identifiable_type "
            + "ua_targetIdentifiableType, ua.target_identifiable_objecttype ua_targetIdentifiableObjectType, ua.target_language ua_targetLanguage, ua.target_uuid ua_targetUuid, "
            + "ua.uuid ua_uuid, ua.website_uuid ua_websiteUuid, webs.uuid webs_uuid, webs.label webs_label, webs.url webs_url "
            + "FROM url_aliases AS ua LEFT JOIN websites webs ON webs.uuid = ua.website_uuid "
            + "WHERE (ua.slug ILIKE '%' || :filtervalue_1 || '%')"
            + ") innerselect_rownumber "
            + "WHERE '(7000,8000]'::int8range @> innerselect_rownumber.rn",
        innerSql.toString());
  }

  private class MyImpl extends JdbiRepositoryImpl {

    public MyImpl() {
      super(null, null, null, null, 5000);
    }

    @Override
    protected List<String> getAllowedOrderByFields() {
      return new ArrayList<>(List.of("last_modified", "slug"));
    }

    @Override
    public String getColumnName(String modelProperty) {
      return modelProperty;
    }

    @Override
    protected String getUniqueField() {
      return "uuid";
    }

    @Override
    protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
      return false;
    }
  }
}
