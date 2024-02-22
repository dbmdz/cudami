package io.github.dbmdz.cudami.converter;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StringToOrderConverterTest {
  StringToOrderConverter converter = new StringToOrderConverter();

  @Test
  public void testConversionNull() {
    Order converted = converter.convert(null);
    assertThat(converted).isNull();
  }

  @Test
  public void testConversionNotMatching() {
    String source = "created.nullfirst";
    Order converted = converter.convert(source);
    assertThat(converted).isNull();

    source = "created.DSC";
    converted = converter.convert(source);
    assertThat(converted).isNull();
  }

  @Test
  public void testConversionProperty() {
    String source = "created";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo(source);
    assertThat(converted.getSubProperty()).isEqualTo(Optional.empty());
    assertThat(converted.getDirection()).isEqualTo(Sorting.DEFAULT_DIRECTION);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NATIVE);
  }

  @Test
  public void testConversionPropertySubProperty() {
    String source = "label_de";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("label");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.of("de"));
    assertThat(converted.getDirection()).isEqualTo(Sorting.DEFAULT_DIRECTION);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NATIVE);
  }

  @Test
  public void testConversionPropertySubProperty2() {
    String source = "label_de-Latn";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("label");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.of("de-Latn"));
    assertThat(converted.getDirection()).isEqualTo(Sorting.DEFAULT_DIRECTION);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NATIVE);
  }

  @Test
  public void testConversionPropertyDirection() {
    String source = "lastModified.desc";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("lastModified");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.empty());
    assertThat(converted.getDirection()).isEqualTo(Direction.DESC);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NATIVE);
  }

  @Test
  public void testConversionPropertyNullHandling() {
    String source = "name.nullsfirst";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("name");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.empty());
    assertThat(converted.getDirection()).isEqualTo(Sorting.DEFAULT_DIRECTION);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NULLS_FIRST);
  }

  @Test
  public void testFullConversion() {
    String source = "label_en.asc.nullslast.ignorecase";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("label");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.of("en"));
    assertThat(converted.getDirection()).isEqualTo(Direction.ASC);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NULLS_LAST);
    assertThat(converted.isIgnoreCase()).isEqualTo(true);
  }

  @Test
  public void testFullConversionWithoutIgnoreCase() {
    String source = "label_en.asc.nullslast";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("label");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.of("en"));
    assertThat(converted.getDirection()).isEqualTo(Direction.ASC);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NULLS_LAST);
    assertThat(converted.isIgnoreCase()).isEqualTo(false);
  }

  @Test
  public void testFullConversionIgnoreCase() {
    String source = "label_en.ASC.NULLSLAST";
    Order converted = converter.convert(source);
    assertThat(converted.getProperty()).isEqualTo("label");
    assertThat(converted.getSubProperty()).isEqualTo(Optional.of("en"));
    assertThat(converted.getDirection()).isEqualTo(Direction.ASC);
    assertThat(converted.getNullHandling()).isEqualTo(NullHandling.NULLS_LAST);
  }
}
