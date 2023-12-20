package de.digitalcollections.model.jackson.identifiable.entity.geo.location;

import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.Lake;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class LakeTest extends BaseJsonSerializationTest {

  private Lake createObject() {
    Lake obj = new Lake();
    obj.setLabel(new LocalizedText(Locale.ENGLISH, "Lake"));
    obj.setCoordinateLocation(new CoordinateLocation(48.15093479009475, 11.52559973769878, 0d, 6d));
    return obj;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Lake geoLocation = createObject();
    checkSerializeDeserialize(
        geoLocation, "serializedTestObjects/identifiable/entity/geo/location/Lake.json");
  }
}
