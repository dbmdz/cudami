package de.digitalcollections.model.jackson.identifiable.entity.geo.location;

import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class MountainTest extends BaseJsonSerializationTest {

  private Mountain createObject() {
    Mountain obj = new Mountain();
    obj.setLabel(new LocalizedText(Locale.ENGLISH, "Mountain"));
    obj.setCoordinateLocation(new CoordinateLocation(48.15093479009475, 11.52559973769878, 0d, 6d));
    obj.setHeight(8999);
    return obj;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Mountain geoLocation = createObject();
    checkSerializeDeserialize(
        geoLocation, "serializedTestObjects/identifiable/entity/geo/location/Mountain.json");
  }
}
