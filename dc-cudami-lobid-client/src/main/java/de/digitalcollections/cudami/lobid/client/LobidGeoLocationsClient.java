package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.mapper.Lobid2DCModelMapper;
import de.digitalcollections.lobid.model.LobidGeoLocation;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidGeoLocationsClient extends LobidBaseClient<LobidGeoLocation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidGeoLocationsClient.class);

  LobidGeoLocationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidGeoLocation.class, mapper);
  }

  public GeoLocation getByGndId(String gndId) throws TechnicalException {
    LobidGeoLocation lobidGeoLocation = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    GeoLocation geoLocation = Lobid2DCModelMapper.mapToGeoLocation(lobidGeoLocation, null);
    return geoLocation;
  }
}
