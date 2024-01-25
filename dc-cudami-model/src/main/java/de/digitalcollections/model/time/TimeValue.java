package de.digitalcollections.model.time;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import org.threeten.extra.chrono.JulianDate;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

/**
 * From interface "org.wikidata.wdtk.datamodel.interfaces.TimeValue":
 *
 * <p>Time values represent points and intervals in time, and additional information about their
 * format. Information includes a specific time point, information about its precision, and the
 * preferred calendar model and timezone (for display). Moreover, time values can describe some
 * uncertainty regarding the exact position in time. This is achieved by tolerance values that
 * specify how much {@link #getBeforeTolerance() before} or {@link #getBeforeTolerance() after} the
 * given time point an event might have occurred.
 *
 * <p>Time points cannot describe durations (which are quantities), recurring events ("1st of May"),
 * or time spans ( "He reigned from 1697 to 1706, i.e., during <i>every</i> moment of that time
 * span" ). Intervals expressed by times always encode uncertainty ("He died in the year 546 BCE,
 * i.e., in <i>some</i> moment within that interval").
 *
 * <p>The main time point of the value generally refers to the proleptic Gregorian calendar.
 * However, if dates are imprecise (like "Jan 1512" or even "1200") then one cannot convert this
 * reliably and Wikidata will just keep the value as entered.
 *
 * <p>"Y0K issue": Neither the Gregorian nor the Julian calendar assume a year 0, i.e., the year 1
 * BCE was followed by 1 CE in these calendars. See <a href="http://en.wikipedia.org/wiki/Year_zero"
 * >http://en.wikipedia.org/wiki/Year_zero</a>. Wikibase internally uses the year 0. This is the
 * same as ISO-8601, where 1 BCE is represented as "0000". However, note that XML Schema dates (1.0
 * and 2.0) do not have a year 0, so in their case 1BCE is represented as "-1". Understanding the
 * difference is relevant for computing leap years, for computing temporal intervals, and for
 * exporting data.
 *
 * <p>Timezone information is to be given in the form of a positive or negative offset with respect
 * to UTC, measured in minutes. This information specifies the timezone that the time should be
 * displayed in when shown to a user. The recorded time point is in UTC, so timezone can be ignored
 * for comparing values. See {@link #getTimezoneOffset()}.
 *
 * @author Markus Kroetzsch
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_INTERFACE")
public class TimeValue implements org.wikidata.wdtk.datamodel.interfaces.TimeValue {

  private int afterTolerance;
  private int beforeTolerance;
  private byte day;
  private byte hour;
  private byte minute;
  private byte month;
  private byte precision;
  private String preferredCalendarModel = TimeValue.CM_GREGORIAN_PRO;
  private byte second;
  private int timezoneOffset;
  private long year;

  public TimeValue() {
    this.afterTolerance = 0;
    this.beforeTolerance = 0;
    this.day = 0;
    this.hour = 0;
    this.minute = 0;
    this.month = 0;
    this.precision = 0;
    this.second = 0;
    this.timezoneOffset = 0;
    this.year = 0;
  }

  public TimeValue(
      long year,
      byte month,
      byte day,
      byte hour,
      byte minute,
      byte second,
      byte precision,
      int beforeTolerance,
      int afterTolerance,
      int timezoneOffset,
      String preferredCalendarModel) {
    this.afterTolerance = afterTolerance;
    this.beforeTolerance = beforeTolerance;
    this.preferredCalendarModel = preferredCalendarModel;
    this.day = day;
    this.hour = hour;
    this.minute = minute;
    this.month = month;
    this.precision = precision;
    this.second = second;
    this.timezoneOffset = timezoneOffset;
    this.year = year;
  }

  public TimeValue(
      long year,
      int month,
      int day,
      int hour,
      int minute,
      int second,
      byte precision,
      int beforeTolerance,
      int afterTolerance,
      int timezoneOffset,
      String preferredCalendarModel) {
    this.afterTolerance = afterTolerance;
    this.beforeTolerance = beforeTolerance;
    this.preferredCalendarModel = preferredCalendarModel;
    this.day = (byte) day;
    this.hour = (byte) hour;
    this.minute = (byte) minute;
    this.month = (byte) month;
    this.second = (byte) second;
    this.precision = precision;
    this.timezoneOffset = timezoneOffset;
    this.year = year;
  }

  public TimeValue(long year, byte month) {
    this();
    this.precision = TimeValue.PREC_MONTH;
    this.year = year;
    this.month = month;
  }

  public TimeValue(long year) {
    this();
    this.precision = TimeValue.PREC_YEAR;
    this.year = year;
  }

  public TimeValue(long year, byte month, byte day) {
    this();
    this.precision = TimeValue.PREC_DAY;
    this.day = day;
    this.month = month;
    this.year = year;
  }

  // TODO Konstuktur mit int values oder besser: builder

  @Override
  public <T> T accept(ValueVisitor<T> valueVisitor) {
    return null;
  }

  @Override
  public int getAfterTolerance() {
    return afterTolerance;
  }

  @Override
  public int getBeforeTolerance() {
    return beforeTolerance;
  }

  @Override
  public byte getDay() {
    return day;
  }

  @Override
  public byte getHour() {
    return hour;
  }

  @Override
  public byte getMinute() {
    return minute;
  }

  @Override
  public byte getMonth() {
    return month;
  }

  @Override
  public byte getPrecision() {
    return precision;
  }

  @Override
  public String getPreferredCalendarModel() {
    return preferredCalendarModel;
  }

  @Override
  public ItemIdValue getPreferredCalendarModelItemId() {
    return null;
  }

  @Override
  public byte getSecond() {
    return second;
  }

  @Override
  public int getTimezoneOffset() {
    return timezoneOffset;
  }

  @Override
  public long getYear() {
    return year;
  }

  public void setAfterTolerance(int afterTolerance) {
    this.afterTolerance = afterTolerance;
  }

  public void setBeforeTolerance(int beforeTolerance) {
    this.beforeTolerance = beforeTolerance;
  }

  public void setDay(byte day) {
    this.day = day;
  }

  public void setHour(byte hour) {
    this.hour = hour;
  }

  public void setMinute(byte minute) {
    this.minute = minute;
  }

  public void setMonth(byte month) {
    this.month = month;
  }

  public void setPrecision(byte precision) {
    this.precision = precision;
  }

  public void setPreferredCalendarModel(String preferredCalendarModel) {
    this.preferredCalendarModel = preferredCalendarModel;
  }

  public void setSecond(byte second) {
    this.second = second;
  }

  public void setTimezoneOffset(int timezoneOffset) {
    this.timezoneOffset = timezoneOffset;
  }

  public void setYear(long year) {
    this.year = year;
  }

  @Override
  public TimeValue toGregorian() {
    // TODO not checked yet, if code is correct (similar to wdtk implementation....)

    // already in Gregorian calendar
    if (this.getPreferredCalendarModel().equals(TimeValue.CM_GREGORIAN_PRO)) {
      return this;
    }

    // convert Julian
    if (this.getPreferredCalendarModel().equals(TimeValue.CM_JULIAN_PRO)
        && this.getPrecision() >= TimeValue.PREC_DAY
        && this.year > Integer.MIN_VALUE
        && this.year < Integer.MAX_VALUE) {
      try {
        final JulianDate julian = JulianDate.of((int) this.year, this.month, this.day);
        final LocalDate date = LocalDate.from(julian);
        return new TimeValue(
            date.getYear(),
            (byte) date.getMonth().getValue(),
            (byte) date.getDayOfMonth(),
            this.hour,
            this.minute,
            this.second,
            this.precision,
            this.beforeTolerance,
            this.afterTolerance,
            this.timezoneOffset,
            TimeValue.CM_GREGORIAN_PRO);
      } catch (DateTimeException e) {
        return null;
      }
    }

    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TimeValue)) {
      return false;
    }
    TimeValue timeValue = (TimeValue) o;
    return afterTolerance == timeValue.afterTolerance
        && beforeTolerance == timeValue.beforeTolerance
        && day == timeValue.day
        && hour == timeValue.hour
        && minute == timeValue.minute
        && month == timeValue.month
        && precision == timeValue.precision
        && second == timeValue.second
        && timezoneOffset == timeValue.timezoneOffset
        && year == timeValue.year
        && Objects.equals(preferredCalendarModel, timeValue.preferredCalendarModel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        afterTolerance,
        beforeTolerance,
        day,
        hour,
        minute,
        month,
        precision,
        preferredCalendarModel,
        second,
        timezoneOffset,
        year);
  }

  @Override
  public String toString() {
    return "TimeValue{"
        + "afterTolerance="
        + afterTolerance
        + ", beforeTolerance="
        + beforeTolerance
        + ", day="
        + day
        + ", hour="
        + hour
        + ", minute="
        + minute
        + ", month="
        + month
        + ", precision="
        + precision
        + ", preferredCalendarModel='"
        + preferredCalendarModel
        + '\''
        + ", second="
        + second
        + ", timezoneOffset="
        + timezoneOffset
        + ", year="
        + year
        + '}';
  }
}
