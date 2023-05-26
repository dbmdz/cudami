package de.digitalcollections.cudami.server.controller.converter;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.TypeDescriptor;

@SpringBootTest
class StringToFilterCriteriaGenericConverterTest {

  @Autowired private StringToFilterCriteriaGenericConverter converter;

  @Test
  @DisplayName("check supported types")
  void checkTypes() {
    assertThat(converter.getConvertibleTypes()).size().isEqualTo(2);
  }

  @Test
  @DisplayName("test complex `Filtering`")
  void testFiltering() {
    Filtering expected =
        Filtering.builder()
            .filterCriterion(
                FilterLogicalOperator.OR,
                new FilterCriterion<String>("label.de-Latn", FilterOperation.CONTAINS, "some text"))
            .filterCriterion(
                FilterLogicalOperator.OR,
                new FilterCriterion<String>(
                    "description.de-Latn", FilterOperation.CONTAINS, "some text"))
            .filterCriterion(
                FilterLogicalOperator.AND,
                new FilterCriterion<String>(
                    "lastModified", FilterOperation.GREATER_THAN, "2020-01-01"))
            .build();
    String source =
        "%7B$OR;label.de-Latn:like:some+text;description.de-Latn:like:some+text%7D;%7BlastModified:gt:2020-01-01%7D";
    Object actual =
        converter.convert(
            source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(Filtering.class));
    assertThat(actual instanceof Filtering).isTrue();
    assertThat(actual).isEqualTo(expected);
  }
}
