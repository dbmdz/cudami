package de.digitalcollections.model.time;

import static de.digitalcollections.model.time.TimestampHelper.truncatedToMicros;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimestampHelper")
public class TimestampHelperTest {

  @Test
  @DisplayName("truncate LocalDateTime to microseconds")
  public void testTruncatedToMicros() {
    var original = LocalDateTime.now().withNano(123456789);
    assertThat(original.toString()).endsWith(".123456789");
    var expected = original.withNano(123456000);
    assertThat(expected.getNano() % 1000).isEqualTo(0);
    assertThat(expected.toString()).endsWith(".123456");
    var actual = truncatedToMicros(original);
    assertThat(actual).isNotSameAs(original).isEqualTo(expected);
  }

  @Test
  @DisplayName("returns argument if already ms")
  public void testIdenticalObjectReturned() {
    var original = LocalDateTime.now().withNano(123456000);
    var actual = truncatedToMicros(original);
    assertThat(actual).isSameAs(original);
  }

  @Test
  @DisplayName("return null for null arg")
  public void testReturnNull() {
    assertThat(truncatedToMicros(null)).isNull();
  }
}
