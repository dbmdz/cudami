package de.digitalcollections.model.identifiable.entity.manifestation;

import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.time.TimeValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * An abstract container, which integrated all publishers, which are responsible in a certain way
 * (e.g. production) for a manifestation, and which also holds information about the time range of
 * that involvement.
 */
@SuperBuilder(buildMethodName = "prebuild")
public abstract class PublishingInfo {

  protected List<Publisher> publishers;
  protected String datePresentation;
  protected LocalDateRange navDateRange;
  protected TimeValueRange timeValueRange;

  public PublishingInfo() {}

  public boolean isEmpty() {
    return (publishers == null || publishers.isEmpty())
        && datePresentation == null
        && navDateRange == null
        && timeValueRange == null;
  }

  public List<Publisher> getPublishers() {
    return publishers;
  }

  public void setPublishers(List<Publisher> publishers) {
    this.publishers = publishers;
  }

  /**
   * The original free text representation of the date or date range
   *
   * @return the textual representation with no restrictions at all
   */
  public String getDatePresentation() {
    return datePresentation;
  }

  /**
   * Used to fill the textual representation of the date or date range
   *
   * @param datePresentation A string with any data, you want
   */
  public void setDatePresentation(String datePresentation) {
    this.datePresentation = datePresentation;
  }

  /**
   * The "navigation" date range - use for sorting and querying, will be filled heuristically
   *
   * @return a filled LocalDateRange
   */
  public LocalDateRange getNavDateRange() {
    return navDateRange;
  }

  /**
   * Used to heuristically fill the "navigation" date range, which is used for sorting and querying
   *
   * @param navDateRange
   */
  public void setNavDateRange(LocalDateRange navDateRange) {
    this.navDateRange = navDateRange;
  }

  /**
   * The date range with implicit precision
   *
   * @return a TimeValueRange
   */
  public TimeValueRange getTimeValueRange() {
    return timeValueRange;
  }

  /**
   * Set the date range with the given implicit precision
   *
   * @param timeValueRange The TimeValueRange
   */
  public void setTimeValueRange(TimeValueRange timeValueRange) {
    this.timeValueRange = timeValueRange;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "publishers="
        + publishers
        + ", datePresentation='"
        + datePresentation
        + '\''
        + ", navDateRange="
        + navDateRange
        + ", timeValueRange="
        + timeValueRange
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PublishingInfo)) {
      return false;
    }
    PublishingInfo that = (PublishingInfo) o;
    return Objects.equals(publishers, that.publishers)
        && Objects.equals(datePresentation, that.datePresentation)
        && Objects.equals(navDateRange, that.navDateRange)
        && Objects.equals(timeValueRange, that.timeValueRange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publishers, datePresentation, navDateRange, timeValueRange);
  }

  public abstract static class PublishingInfoBuilder<
      C extends PublishingInfo, B extends PublishingInfoBuilder<C, B>> {

    public B publisher(Publisher publisher) {
      if (publishers == null) {
        publishers = new ArrayList<>(1);
      }
      publishers.add(publisher);
      return self();
    }

    public C build() {
      C c = prebuild();
      return c;
    }
  }
}
