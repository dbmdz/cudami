package de.digitalcollections.model.list.sorting;

import java.util.Locale;

/**
 * Enumeration for sort directions. See Spring Data Commons, but more flat design and independent of
 * Spring libraries.
 */
public enum Direction {
  ASC,
  DESC;

  /**
   * @return true if direction is ascending
   */
  public boolean isAscending() {
    return this.equals(ASC);
  }

  /**
   * @return true if direction is ascending
   */
  public boolean isDescending() {
    return this.equals(DESC);
  }

  /**
   * Returns the {@link Direction} enum for the given {@link String} value.
   *
   * @param value given direction value
   * @return the direction enumeration
   * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
   */
  public static Direction fromString(String value) {

    try {
      return Direction.valueOf(value.toUpperCase(Locale.US));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).",
              value),
          e);
    }
  }

  /**
   * Returns the {@link Direction} enum for the given {@link String} or null if it cannot be parsed
   * into an enum value.
   *
   * @param value given direction value
   * @return the appropriate direction enum or null
   */
  public static Direction fromStringOrNull(String value) {

    try {
      return fromString(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
