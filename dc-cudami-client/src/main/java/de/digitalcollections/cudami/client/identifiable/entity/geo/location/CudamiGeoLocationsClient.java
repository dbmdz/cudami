package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiGeoLocationsClient extends CudamiEntitiesClient<GeoLocation> {

  public CudamiGeoLocationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GeoLocation.class, mapper, API_VERSION_PREFIX + "/geolocations");
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }
}
