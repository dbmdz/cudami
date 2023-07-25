package de.digitalcollections.cudami.lobid.client.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.lobid.model.LobidGeometry;
import de.digitalcollections.model.geo.CoordinateLocation;
import org.junit.jupiter.api.Test;

class Lobid2DCModelMapperTest {

  @Test
  void testMapGeometryToCoordinateLocation() {
    LobidGeometry lobidGeometry = new LobidGeometry();
    lobidGeometry.setType("Point");
    lobidGeometry.setAsWKT(new String[] {"Point ( +012.573850 -048.881259 )"});
    CoordinateLocation coordinateLocation =
        Lobid2DCModelMapper.mapGeometryToCoordinateLocation(lobidGeometry);
    assertEquals(coordinateLocation.getLatitude(), -48.881259d);
    assertEquals(coordinateLocation.getLongitude(), 12.573850d);
  }
}
