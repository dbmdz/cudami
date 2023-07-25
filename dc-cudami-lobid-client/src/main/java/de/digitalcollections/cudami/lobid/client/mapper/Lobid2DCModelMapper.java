package de.digitalcollections.cudami.lobid.client.mapper;

import de.digitalcollections.lobid.model.LobidCorporateBody;
import de.digitalcollections.lobid.model.LobidDepiction;
import de.digitalcollections.lobid.model.LobidEntity;
import de.digitalcollections.lobid.model.LobidEvent;
import de.digitalcollections.lobid.model.LobidGeoLocation;
import de.digitalcollections.lobid.model.LobidGeometry;
import de.digitalcollections.lobid.model.LobidHomepage;
import de.digitalcollections.lobid.model.LobidPerson;
import de.digitalcollections.lobid.model.LobidSubject;
import de.digitalcollections.lobid.model.LobidWork;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.Title;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

public class Lobid2DCModelMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(Lobid2DCModelMapper.class);

  private static byte calculateDatePrecision(String dateStr) {
    // The number of "X" values determines the precision
    int numberOfX = StringUtils.countMatches(dateStr.toUpperCase(Locale.ROOT), "X");
    switch (numberOfX) {
      case 0:
        return TimeValue.PREC_YEAR;
      case 1:
        return TimeValue.PREC_DECADE;
      case 2:
        return TimeValue.PREC_100Y;
      case 3:
        return TimeValue.PREC_1KY;
      default:
        return TimeValue.PREC_1GY; // As imprecise, as possible
    }
  }

  private static String fixForEndDate(String endDate) {
    return endDate.replaceAll("[xX]", "9").replaceAll("[^0-9-]", "");
  }

  private static String fixForStartDate(String startDate) {
    return startDate.replaceAll("[xX]", "0").replaceAll("[^0-9-]", "");
  }

  private static ImageFileResource mapDepictionToImageFileResource(List<LobidDepiction> depiction) {
    if (depiction != null && !depiction.isEmpty()) {
      String thumbnailUrl = depiction.get(0).getThumbnail();
      ImageFileResource imageFileResource = new ImageFileResource();
      imageFileResource.setUri(URI.create(thumbnailUrl));
      imageFileResource.setMimeType(MimeType.MIME_IMAGE);
      return imageFileResource;
    }
    return null;
  }

  private static URL mapHomepageToUrl(List<LobidHomepage> homepage) {
    if (homepage != null && !homepage.isEmpty()) {
      String homepageId = homepage.get(0).getId();
      try {
        return URI.create(homepageId).toURL();
      } catch (MalformedURLException ex) {
        LOGGER.warn("Invalid homepage URL: " + homepageId);
      }
    }
    return null;
  }

  public static CorporateBody mapToCorporateBody(
      LobidCorporateBody lobidCorporateBody, CorporateBody corporateBody) {
    if (corporateBody == null) {
      corporateBody = new CorporateBody();
    }
    mapToIdentifiable(lobidCorporateBody, corporateBody);
    mapToNamedEntity(lobidCorporateBody, corporateBody);

    // label
    String label = lobidCorporateBody.getPreferredName();
    List<String> abbreviatedNameForTheCorporateBody =
        lobidCorporateBody.getAbbreviatedNameForTheCorporateBody();
    if (label != null && abbreviatedNameForTheCorporateBody != null) {
      label = label + " (" + abbreviatedNameForTheCorporateBody.get(0) + ")";
    }
    corporateBody.setLabel(new LocalizedText(Locale.GERMAN, label));

    // depiction -> preview image = logo
    List<LobidDepiction> depiction = lobidCorporateBody.getDepiction();
    ImageFileResource imageFileResource = mapDepictionToImageFileResource(depiction);
    if (imageFileResource != null) {
      imageFileResource.setLabel(new LocalizedText(Locale.GERMAN, "Logo " + label));
      corporateBody.setPreviewImage(imageFileResource);
    }

    // homepage
    List<LobidHomepage> homepage = lobidCorporateBody.getHomepage();
    URL homepageUrl = mapHomepageToUrl(homepage);
    corporateBody.setHomepageUrl(homepageUrl);

    return corporateBody;
  }

  public static Event mapToEvent(LobidEvent lobidEvent, Event event) {
    if (event == null) {
      event = new Event();
    }
    mapToIdentifiable(lobidEvent, event);
    mapToNamedEntity(lobidEvent, event);
    return event;
  }

  public static GeoLocation mapToGeoLocation(
      LobidGeoLocation lobidGeoLocation, GeoLocation geoLocation) {
    if (geoLocation == null) {
      geoLocation = new GeoLocation();
    }
    mapToIdentifiable(lobidGeoLocation, geoLocation);
    mapToNamedEntity(lobidGeoLocation, geoLocation);

    // depiction -> preview image = portrait
    List<LobidDepiction> depiction = lobidGeoLocation.getDepiction();
    ImageFileResource imageFileResource = mapDepictionToImageFileResource(depiction);
    if (imageFileResource != null) {
      imageFileResource.setLabel(geoLocation.getLabel());
      geoLocation.setPreviewImage(imageFileResource);
    }

    // homepage (TODO: not supported yet in dc model for geolocation)
    //        List<LobidHomepage> homepage = lobidGeoLocation.getHomepage();
    //        URL homepageUrl = mapHomepageToUrl(homepage);
    //        geoLocation.setHomepageUrl(homepageUrl);

    // geo coordinates
    if (lobidGeoLocation.getHasGeometry() != null && !lobidGeoLocation.getHasGeometry().isEmpty()) {
      LobidGeometry lobidGeometry = lobidGeoLocation.getHasGeometry().get(0);
      try {
        CoordinateLocation coordinateLocation = mapGeometryToCoordinateLocation(lobidGeometry);
        geoLocation.setCoordinateLocation(coordinateLocation);
      } catch (Exception e) {
        LOGGER.warn("Can not parse CoordinateLocation from LobidGeometry " + lobidGeometry, e);
      }
    }
    return geoLocation;
  }

  public static CoordinateLocation mapGeometryToCoordinateLocation(LobidGeometry lobidGeometry) {
    if ("Point".equals(lobidGeometry.getType()) && lobidGeometry.getAsWKT() != null) {
      String wkt = lobidGeometry.getAsWKT()[0];
      // Point ( +012.573850 +048.881259 ): longitude, latitude, precision:
      wkt = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")")).trim();
      String longitude = wkt.split(" ")[0];
      Double lon = Double.parseDouble(longitude);
      String latitude = wkt.split(" ")[1];
      Double lat = Double.parseDouble(latitude);
      CoordinateLocation cl = new CoordinateLocation(lat, lon, 0d, 0.000001d);

      // TODO: maybe make use of library (already in pom.xml)
      // https://docs.geotools.org/latest/userguide/library/main/geometry.html
      // see WKT: Well-Known Text (WKT),
      // https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry#Geometric_objects
      return cl;
    }

    return null;
  }

  public static HumanSettlement mapToHumanSettlement(
      LobidGeoLocation lobidGeoLocation, HumanSettlement humanSettlement) {
    if (humanSettlement == null) {
      humanSettlement = new HumanSettlement();
    }
    mapToGeoLocation(lobidGeoLocation, humanSettlement);
    return humanSettlement;
  }

  public static Identifiable mapToIdentifiable(LobidEntity lobidEntity, Identifiable identifiable) {
    if (identifiable == null) {
      identifiable = new Identifiable();
    }

    // identifier
    String gndIdentifier = lobidEntity.getGndIdentifier();
    if (gndIdentifier != null) {
      identifiable.addIdentifier(Identifier.builder().namespace("gnd").id(gndIdentifier).build());
    }

    // label
    String label = lobidEntity.getPreferredName();
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, label));

    return identifiable;
  }

  public static NamedEntity mapToNamedEntity(LobidEntity lobidEntity, NamedEntity namedEntity) {
    if (namedEntity == null) {
      throw new IllegalArgumentException("NamedEntity must not be null");
    }

    // name
    String name = lobidEntity.getPreferredName();
    namedEntity.setName(new LocalizedText(Locale.GERMAN, name));

    return namedEntity;
  }

  public static Person mapToPerson(LobidPerson lobidPerson, Person person) {
    if (person == null) {
      person = new Person();
    }
    mapToIdentifiable(lobidPerson, person);
    mapToNamedEntity(lobidPerson, person);

    // date of birth
    String dateOfBirth = lobidPerson.getDateOfBirth();
    if (dateOfBirth != null && !dateOfBirth.isBlank()) {
      byte precision = calculateDatePrecision(dateOfBirth);
      dateOfBirth = fixForStartDate(dateOfBirth);
      person.setDateOfBirth(DateUtil.extractFilledStartDate(dateOfBirth));
      try {
        person.setTimeValueOfBirth(DateUtil.extractTimeValue(dateOfBirth, precision));
      } catch (ParseException e) {
        LOGGER.warn("Can not set TimeValue of birth.", e);
      }
    }

    // date of death
    String dateOfDeath = lobidPerson.getDateOfDeath();
    if (dateOfDeath != null && !dateOfDeath.isBlank()) {
      byte precision = calculateDatePrecision(dateOfDeath);
      dateOfDeath = fixForEndDate(dateOfDeath);
      person.setDateOfDeath(DateUtil.extractFilledEndDate(dateOfDeath));
      try {
        person.setTimeValueOfDeath(DateUtil.extractTimeValue(dateOfDeath, precision));
      } catch (ParseException e) {
        LOGGER.warn("Can not set TimeValue of death.", e);
      }
    }

    // depiction -> preview image = portrait
    List<LobidDepiction> depiction = lobidPerson.getDepiction();
    ImageFileResource imageFileResource = mapDepictionToImageFileResource(depiction);
    if (imageFileResource != null) {
      imageFileResource.setLabel(person.getLabel());
      person.setPreviewImage(imageFileResource);
    }
    return person;
  }

  public static Subject mapToSubject(LobidSubject lobidSubject, Subject subject) {
    if (subject == null) {
      subject = new Subject();
    }
    mapToIdentifiable(lobidSubject, subject);
    return subject;
  }

  public static Work mapToWork(LobidWork lobidWork, Work work) {
    if (work == null) {
      work = new Work();
    }
    mapToIdentifiable(lobidWork, work);

    // title
    work.setTitles(List.of(Title.builder().text(work.getLabel()).build()));
    return work;
  }
}
