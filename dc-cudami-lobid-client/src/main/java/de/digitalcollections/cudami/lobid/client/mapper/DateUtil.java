package de.digitalcollections.cudami.lobid.client.mapper;

import de.digitalcollections.model.time.TimeValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {

  public static LocalDate extractFilledStartDate(String dateString) {
    return extractFilledDate(dateString, 1, 1);
  }

  public static LocalDate extractFilledEndDate(String dateString) {
    return extractFilledDate(dateString, 12, 31);
  }

  private static LocalDate extractFilledDate(
      String dateString, int monthIfNotGiven, int dayIfNotGiven) {
    if (dateString == null || dateString.isBlank()) {
      return null; // we cannot improve anything here
    }

    boolean isBC = dateString.startsWith("-");

    String filledDateString;
    int year;

    try {
      String[] dateStringParts = dateString.replaceFirst("^-", "").split("-");
      switch (dateStringParts.length) {
        case 1: // Only year given, fill up month and day!
          year = Integer.parseInt(dateStringParts[0]);
          filledDateString =
              String.format(
                  "%04d-%02d-%02d %s", year, monthIfNotGiven, dayIfNotGiven, (isBC ? "BC" : "AD"));
          break;
        case 2: // Only year and month given; fill up the day (and ensure, that it never exceeds the
          // max. possible day in a month
          year = Integer.parseInt(dateStringParts[0]);
          int month = Integer.parseInt(dateStringParts[1]);
          int day = Math.min(dayIfNotGiven, maxDayInMonth(year, month));
          filledDateString =
              String.format("%04d-%02d-%02d %s", year, month, day, (isBC ? "BC" : "AD"));
          break;
        case 3: // Year, month and day given. Perfect!
          filledDateString = String.format("%s %s", dateString, (isBC ? "BC" : "AD"));
          break;
        default:
          throw new IllegalArgumentException(
              "Invalid date='" + dateString + "' detected. Giving up here.");
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Cannot parse date='" + dateString + "'", e);
    }

    return LocalDate.parse(
        filledDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd GG", Locale.ROOT));
  }

  private static int maxDayInMonth(int year, int month) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, month - 1); // bad pitfall ;-)
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
    return cal.get(Calendar.DAY_OF_MONTH);
  }

  @SuppressFBWarnings(
      value = "REC_CATCH_EXCEPTION",
      justification = "We really want to catch any error")
  public static TimeValue extractTimeValue(String dateString, byte leastPrecision)
      throws ParseException {
    if (dateString == null || dateString.isBlank()) {
      return null;
    }
    try {
      boolean yearBC = false;
      if (dateString.startsWith("-")) {
        yearBC = true;
        dateString = dateString.substring(1);
      }
      String[] parts = dateString.split("-");

      TimeValue tv;

      switch (parts.length) {
        case 1: // year only or evel less precise
          tv = new TimeValue(Integer.parseInt(parts[0]) * (yearBC ? -1 : 1));
          tv.setPrecision(leastPrecision);
          break;
        case 2: // year and month
          tv =
              new TimeValue(
                  Long.parseLong(parts[0]) * (yearBC ? -1 : 1), Byte.parseByte(parts[1]), (byte) 0);
          tv.setPrecision(org.wikidata.wdtk.datamodel.interfaces.TimeValue.PREC_MONTH);
          break;
        case 3:
          tv =
              new TimeValue(
                  Long.parseLong(parts[0]) * (yearBC ? -1 : 1),
                  Byte.parseByte(parts[1]),
                  Byte.parseByte(parts[2]));
          tv.setPrecision(org.wikidata.wdtk.datamodel.interfaces.TimeValue.PREC_DAY);
          break;
        default:
          throw new ParseException("Unknown format for date='" + dateString + "'", 0);
      }

      return tv;
    } catch (Exception e) {
      throw new ParseException("Invalid format for date='" + dateString + "'", 0);
    }
  }
}
