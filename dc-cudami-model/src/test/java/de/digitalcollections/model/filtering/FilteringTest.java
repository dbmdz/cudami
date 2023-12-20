package de.digitalcollections.model.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class FilteringTest<T extends Object> {

  final LocalDate maxDate = LocalDate.parse("2200-12-31");
  final LocalDate minDate = LocalDate.parse("1970-01-01");

  public FilteringTest() {}

  @Test
  public void testGetFilterCriteriaFor() {
    String property = "publicationStart";
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(property)
                    .between(minDate, maxDate)
                    .build())
            .build();

    // Filtering.defaultBuilder().filter(property).between(minDate, maxDate).build();
    FilterCriterion fc = filtering.getFilterCriterionFor(property);
    assertEquals(fc.getMinValue(), minDate);
    assertEquals(fc.getMaxValue(), maxDate);
  }

  @Test
  public void testOperationEquals() {
    String property = "publicationStart";
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(property).isEquals(minDate).build())
            .build();
    FilterCriterion fc = filtering.getFilterCriterionFor(property);
    assertEquals(fc.getValue(), minDate);
  }

  @Test
  public void testOperationInWithDifferentValueObjects() {
    String property = "publicationStart";

    // String[]
    String[] values1 = new String[] {"eins", "zwei", "drei"};
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(property)
                    .in(Arrays.asList(values1))
                    .build())
            .build();
    FilterCriterion fc = filtering.getFilterCriterionFor(property);
    assertEquals(fc.getValues().size(), values1.length);

    // ArrayList
    ArrayList<String> values2 = new ArrayList<>(0);
    values2.add("eins");
    values2.add("zwei");
    values2.add("drei");
    filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression(property).in(values2).build())
            .build();
    fc = filtering.getFilterCriterionFor(property);
    assertEquals(fc.getValues().size(), values2.size());
  }
}
