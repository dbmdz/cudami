package de.digitalcollections.model.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The FilterCriterion")
public class FilterCriterionTest {

  @Test
  public void testAutoCorrectionOfMinMaxBetweenValues() throws Exception {
    FilterCriterion filterCriterion =
        new FilterCriterion(
            "dateField",
            FilterOperation.BETWEEN,
            null,
            LocalDate.parse("2020-01-31"),
            LocalDate.parse("2020-01-01"),
            null);

    Filtering filtering = Filtering.builder().add(filterCriterion).build();
    FilterCriterion dateFieldCriteria = filtering.getFilterCriterionFor("dateField");
    assertTrue(
        ((ChronoLocalDate) dateFieldCriteria.getMinValue())
            .isBefore(((ChronoLocalDate) dateFieldCriteria.getMaxValue())));
  }

  @Test
  public void testConstructorValidation() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new FilterCriterion(
                  "dateField",
                  FilterOperation.BETWEEN,
                  null,
                  LocalDate.parse("2020-01-31"),
                  LocalDate.parse("2020-01-01"),
                  Arrays.asList("1"));
            });
    String expectedMessage = "operation requires exactly one min and one max value!";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.equals(expectedMessage));

    exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new FilterCriterion(
                  "dateField",
                  FilterOperation.NOT_SET,
                  null,
                  LocalDate.parse("2020-01-31"),
                  null,
                  null);
            });
    expectedMessage = "operation does not support operand values!";
    actualMessage = exception.getMessage();
    assertTrue(actualMessage.equals(expectedMessage));
  }

  @DisplayName("can filter for list equality")
  @Test
  public void listEquality() {
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("foo")
                    .in(List.of("one", "two", "three"))
                    .build())
            .build();

    assertThat(filtering.getFilterCriteriaList().get(0).get(0).toString())
        .isEqualTo("foo=in:one,two,three");
  }
}
