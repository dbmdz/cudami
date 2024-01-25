package de.digitalcollections.model.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import org.junit.jupiter.api.Test;

public class FilterCriterionBuilderTest {

  public FilterCriterionBuilderTest() {}

  @Test
  public void testIsEquals() {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("test").isEquals(null).build())
            .build();
    assertEquals(
        FilterOperation.NOT_SET, filtering.getFilterCriteriaList().get(0).get(0).getOperation());
  }

  @Test
  public void testNotEquals() {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("test").notEquals(null).build())
            .build();
    assertEquals(
        FilterOperation.SET, filtering.getFilterCriteriaList().get(0).get(0).getOperation());
  }
}
