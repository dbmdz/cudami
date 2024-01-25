package de.digitalcollections.model.jackson.identifiable.entity.geo.location;

import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class GeoLocationTest extends BaseJsonSerializationTest {

  private GeoLocation createObject() {
    GeoLocation geoLocation = new GeoLocation();
    geoLocation.setLabel(new LocalizedText(Locale.GERMAN, "Homebase"));
    geoLocation.setCoordinateLocation(
        new CoordinateLocation(48.15093479009475, 11.52559973769878, 0d, 6d));
    return geoLocation;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    GeoLocation geoLocation = createObject();
    checkSerializeDeserialize(
        geoLocation, "serializedTestObjects/identifiable/entity/geo/location/GeoLocation.json");
  }
}
