package de.digitalcollections.model.time;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimestampHelper {

  /**
   * Truncate the time part of a {@code LocalDateTime} to microseconds.
   *
   * <p>In Java 17 the resolution of time objects increased from microseconds to nanoseconds. Java
   * always stored a time with nanosecond precision but the last three digits have been zero. With
   * the increase of resolution we are facing trouble with the persistence layer: most databases
   * only support microseconds, so does ours. As a consequence the original object and the one
   * retrieved from the database are different -- they differ in 1000 nanoseconds. To overcome this
   * inconsistency the time values must be truncated to microseconds, the resolution that the
   * database can store.
   *
   * @param value
   * @return the {@code LocalDateTime} truncated to microseconds
   */
  public static LocalDateTime truncatedToMicros(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.getNano() % 1000 == 0 ? value : value.truncatedTo(ChronoUnit.MICROS);
  }
}
