package de.digitalcollections.model.time;

import java.time.LocalDate;
import java.util.Objects;

public class LocalDateRange {

  private LocalDate end;
  private LocalDate start;

  public LocalDateRange() {}

  public LocalDateRange(LocalDate start, LocalDate end) {
    this.end = end;
    this.start = start;
  }

  public LocalDate getEnd() {
    return end;
  }

  public LocalDate getStart() {
    return start;
  }

  public void setEnd(LocalDate end) {
    this.end = end;
  }

  public void setStart(LocalDate start) {
    this.start = start;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocalDateRange)) {
      return false;
    }
    LocalDateRange that = (LocalDateRange) o;
    return Objects.equals(end, that.end) && Objects.equals(start, that.start);
  }

  @Override
  public int hashCode() {
    return Objects.hash(end, start);
  }

  @Override
  public String toString() {
    return "LocalDateRange{" + "end=" + end + ", start=" + start + '}';
  }
}
