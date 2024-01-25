package de.digitalcollections.model.identifiable.entity.digitalobject;

import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import java.time.LocalDate;
import java.util.Objects;

/** Details (who, when and where) about the creation of the digital object. */
public class CreationInfo {

  public static Builder builder() {
    return new Builder();
  }

  /** The creator of the digital object */
  private Agent creator;

  /** The date, when the digital object was created */
  private LocalDate date;

  /** The geolocation, where the creation of the digital object took geolocation */
  private GeoLocation geoLocation;

  /**
   * @return the creator of the digital object
   */
  public Agent getCreator() {
    return creator;
  }

  /**
   * @return the date, when the creation of the digital object happened
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * @return the geolocation, where the creation of the digital object took geolocation
   */
  public GeoLocation getGeoLocation() {
    return geoLocation;
  }

  /**
   * Specify, who created the digital object
   *
   * @param creator the creator
   */
  public void setCreator(Agent creator) {
    this.creator = creator;
  }

  /**
   * Set the date, when then creation of the digital object happened
   *
   * @param date the date
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Set the geolocation, where the creation of the digital object took geolocation
   *
   * @param geoLocation the geolocation
   */
  public void setGeoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
  }

  @Override
  public String toString() {
    return "CreationInfo{"
        + "geoLocation="
        + geoLocation
        + ", date="
        + date
        + ", creator="
        + creator
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || !(obj instanceof CreationInfo)) return false;
    CreationInfo that = (CreationInfo) obj;
    return Objects.equals(creator, that.creator)
        && Objects.equals(date, that.date)
        && Objects.equals(geoLocation, that.geoLocation);
  }

  @Override
  public int hashCode() {
    return 1217 + Objects.hash(creator, date, geoLocation);
  }

  public static class Builder {

    CreationInfo creationInfo = new CreationInfo();

    public CreationInfo build() {
      return creationInfo;
    }

    public Builder creator(Agent creator) {
      creationInfo.setCreator(creator);
      return this;
    }

    public Builder date(String date) {
      creationInfo.setDate(LocalDate.parse(date));
      return this;
    }

    public Builder geoLocation(GeoLocation geoLocation) {
      creationInfo.setGeoLocation(geoLocation);
      return this;
    }
  }
}
