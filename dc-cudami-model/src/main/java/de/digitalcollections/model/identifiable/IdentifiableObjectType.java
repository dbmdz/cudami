package de.digitalcollections.model.identifiable;

import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.Canyon;
import de.digitalcollections.model.identifiable.entity.geo.location.Cave;
import de.digitalcollections.model.identifiable.entity.geo.location.Continent;
import de.digitalcollections.model.identifiable.entity.geo.location.Country;
import de.digitalcollections.model.identifiable.entity.geo.location.Creek;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.geo.location.Lake;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import de.digitalcollections.model.identifiable.entity.geo.location.Ocean;
import de.digitalcollections.model.identifiable.entity.geo.location.River;
import de.digitalcollections.model.identifiable.entity.geo.location.Sea;
import de.digitalcollections.model.identifiable.entity.geo.location.StillWaters;
import de.digitalcollections.model.identifiable.entity.geo.location.Valley;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.identifiable.web.Webpage;

public enum IdentifiableObjectType {
  AGENT(Agent.class),
  APPLICATION_FILE_RESOURCE(ApplicationFileResource.class),
  ARTICLE(Article.class),
  AUDIO_FILE_RESOURCE(AudioFileResource.class),
  CANYON(Canyon.class),
  CAVE(Cave.class),
  COLLECTION(Collection.class),
  CONTINENT(Continent.class),
  CORPORATE_BODY(CorporateBody.class),
  COUNTRY(Country.class),
  CREEK(Creek.class),
  DIGITAL_OBJECT(DigitalObject.class),
  ENTITY(Entity.class),
  EVENT(Event.class),
  FAMILY(Family.class),
  FAMILY_NAME(FamilyName.class),
  FILE_RESOURCE(FileResource.class),
  GEO_LOCATION(GeoLocation.class),
  GIVEN_NAME(GivenName.class),
  HEADWORD_ENTRY(HeadwordEntry.class),
  HUMAN_SETTLEMENT(HumanSettlement.class),
  IDENTIFIABLE(Identifiable.class),
  IMAGE_FILE_RESOURCE(ImageFileResource.class),
  ITEM(Item.class),
  LAKE(Lake.class),
  LINKED_DATA_FILE_RESOURCE(LinkedDataFileResource.class),
  MANIFESTATION(Manifestation.class),
  MOUNTAIN(Mountain.class),
  OCEAN(Ocean.class),
  PERSON(Person.class),
  PROJECT(Project.class),
  RIVER(River.class),
  SEA(Sea.class),
  STILL_WATERS(StillWaters.class),
  SUBJECT(Subject.class),
  TEXT_FILE_RESOURCE(TextFileResource.class),
  TOPIC(Topic.class),
  VALLEY(Valley.class),
  VIDEO_FILE_RESOURCE(VideoFileResource.class),
  WEBPAGE(Webpage.class),
  WEBSITE(Website.class),
  WORK(Work.class);

  private final Class<? extends Identifiable> objectClass;

  private IdentifiableObjectType(Class<? extends Identifiable> objectClass) {
    this.objectClass = objectClass;
  }

  public static IdentifiableObjectType getByClass(Class<? extends Identifiable> objectClass) {
    IdentifiableObjectType[] values = IdentifiableObjectType.values();
    for (IdentifiableObjectType value : values) {
      if (value.getObjectClass() == objectClass) {
        return value;
      }
    }
    return null;
  }

  public Class<? extends Identifiable> getObjectClass() {
    return objectClass;
  }

  @Override
  public String toString() {
    return name();
  }
}
