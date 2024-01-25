package de.digitalcollections.model.time;

import java.time.LocalDate;

public enum TimeBasedStatus {
  NOT_YET_IN_RANGE,
  IS_IN_RANGE,
  NO_LONGER_IN_RANGE;

  public static TimeBasedStatus get(LocalDateRange dateRange, LocalDate referenceDate) {
    if (dateRange == null || referenceDate == null) {
      throw new IllegalArgumentException("dateRange and referenceDate must not be null");
    }
    if (dateRange.getStart() != null
        && dateRange.getEnd() != null
        && dateRange.getStart().isAfter(dateRange.getEnd())) {
      throw new IllegalArgumentException("start of dateRange must be before end of dateRange");
    }
    LocalDate startOfRange = dateRange.getStart();
    LocalDate endOfRange = dateRange.getEnd();

    if (startOfRange == null || startOfRange.isAfter(referenceDate)) {
      return NOT_YET_IN_RANGE;
    } else if (endOfRange != null && endOfRange.isBefore(referenceDate)) {
      return NO_LONGER_IN_RANGE;
    } else {
      return IS_IN_RANGE;
    }
  }
}
