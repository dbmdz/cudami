package de.digitalcollections.model.jackson.identifiable.entity.digitalobject;

import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.CreationInfo;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class DigitalObjectTest extends BaseJsonSerializationTest {

  private DigitalObject createObject() {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.addIdentifier(Identifier.builder().namespace("myId").id("bsb10001234").build());
    ImageFileResource image = new ImageFileResource();
    image.setHeight(100);
    image.setWidth(400);
    digitalObject.getFileResources().add(image);
    digitalObject.setPreviewImage(image);
    ApplicationFileResource appFile = new ApplicationFileResource();
    appFile.setFilename("dings.pdf");
    digitalObject.getFileResources().add(appFile);
    digitalObject.setCustomAttribute("companySpecificAttributeX", "foobar");

    CreationInfo creationInfo = new CreationInfo();
    Agent creator = new Person();
    creator.setLabel("Creator");
    creationInfo.setCreator(creator);
    GeoLocation geoLocation = new HumanSettlement();
    geoLocation.setLabel("Geolocation");
    creationInfo.setGeoLocation(geoLocation);
    creationInfo.setDate(LocalDate.of(2021, 12, 1));
    digitalObject.setCreationInfo(creationInfo);

    digitalObject.setNumberOfBinaryResources(42);
    return digitalObject;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    DigitalObject digitalObject = createObject();
    checkSerializeDeserialize(
        digitalObject,
        "serializedTestObjects/identifiable/entity/digitalobject/DigitalObject.json");
  }
}
