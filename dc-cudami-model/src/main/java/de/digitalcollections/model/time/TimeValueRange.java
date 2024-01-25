package de.digitalcollections.model.time;

import java.util.Objects;

public class TimeValueRange {

  private TimeValue end;
  private TimeValue start;

  public TimeValueRange() {}

  public TimeValueRange(TimeValue start, TimeValue end) {
    this.end = end;
    this.start = start;
  }

  public TimeValue getEnd() {
    return end;
  }

  public TimeValue getStart() {
    return start;
  }

  public void setEnd(TimeValue end) {
    this.end = end;
  }

  public void setStart(TimeValue start) {
    this.start = start;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TimeValueRange)) {
      return false;
    }
    TimeValueRange that = (TimeValueRange) o;
    return Objects.equals(end, that.end) && Objects.equals(start, that.start);
  }

  @Override
  public int hashCode() {
    return Objects.hash(end, start);
  }

  @Override
  public String toString() {
    return "TimeValueRange{" + "end=" + end + ", start=" + start + '}';
  }
}
