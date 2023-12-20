package de.digitalcollections.model.identifiable.entity.manifestation;

import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * A Publisher is kind of involvement, consisting of the agent (zero or one), a list of locations
 * (or zero locations) and a date (can be empty, too), which is relevant for this publisher
 * involvement.
 *
 * <p>A Publisher can be an organization, which is active at multiple places and during a certain
 * period of time.
 *
 * <p>Just think of a (here fictive) publishing house, e.g. "Karl-Ranseier-Verlag", which had
 * dependencies in Cologne and Berlin and was active between 1994 and 1998.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Publisher {

  private List<HumanSettlement> locations;
  private Agent agent;

  private String datePresentation;

  public Publisher() {}

  public Publisher(List<HumanSettlement> locations, Agent agent, String datePresentation) {
    this();
    this.locations = locations;
    this.agent = agent;
    this.datePresentation = datePresentation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Publisher)) {
      return false;
    }
    Publisher publisher = (Publisher) o;
    return Objects.equals(locations, publisher.locations)
        && Objects.equals(agent, publisher.agent)
        && Objects.equals(datePresentation, publisher.datePresentation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(locations, agent, datePresentation);
  }

  /**
   * Returns the locations. Currently, only the name of the HumanSettlements is relevant, what
   * means, that these HumanSettlemens won't be reused.
   *
   * @return List of locations where only the names are relevant
   */
  public List<HumanSettlement> getLocations() {
    return locations;
  }

  /**
   * Returns the agent. Currently, only the name is relevant, what means, that you cannot reuse the
   * agent
   *
   * @return the agent where only the name is relevant
   */
  public Agent getAgent() {
    return agent;
  }

  public void setLocations(List<HumanSettlement> locations) {
    this.locations = locations;
  }

  public void addLocation(HumanSettlement location) {
    if (locations == null) {
      locations = new ArrayList<>(1);
    }
    locations.add(location);
  }

  public void setAgent(Agent agent) {
    this.agent = agent;
  }

  /**
   * Used for the textual (free text, no limitations) represenation of the date for the publisher.
   *
   * @return an unformatted, unspecified string
   */
  public String getDatePresentation() {
    return datePresentation;
  }

  public void setDatePresentation(String datePresentation) {
    this.datePresentation = datePresentation;
  }

  @Override
  public String toString() {
    return "Publisher{"
        + "locations="
        + locations
        + ", agent="
        + agent
        + ", datePresentation='"
        + datePresentation
        + '\''
        + '\''
        + '}';
  }

  public abstract static class PublisherBuilder<
      C extends Publisher, B extends PublisherBuilder<C, B>> {

    public C build() {
      C c = prebuild();
      return c;
    }

    public B location(HumanSettlement location) {
      if (locations == null) {
        locations = new ArrayList<>(1);
      }
      locations.add(location);
      return self();
    }
  }
}
