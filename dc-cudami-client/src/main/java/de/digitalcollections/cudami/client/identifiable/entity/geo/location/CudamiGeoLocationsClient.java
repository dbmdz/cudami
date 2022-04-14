package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiGeoLocationsClient extends CudamiEntitiesClient<GeoLocation> {

  public CudamiGeoLocationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GeoLocation.class, mapper, "/v5/geolocations");
  }

  @Override
  public SearchPageResponse<GeoLocation> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    // Interestingly without "/search" in the path
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public List<Locale> findLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }
}
