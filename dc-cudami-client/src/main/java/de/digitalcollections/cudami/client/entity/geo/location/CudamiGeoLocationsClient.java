package de.digitalcollections.cudami.client.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiGeoLocationsClient extends CudamiBaseClient<GeoLocation> {

  public CudamiGeoLocationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GeoLocation.class, mapper);
  }

  public GeoLocation create() {
    return new GeoLocation();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/geolocations/count"));
  }

  public PageResponse<GeoLocation> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/geolocations", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/geolocations", pageRequest, language, initial);
  }

  public PageResponse<GeoLocation> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/latest/geolocations",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public GeoLocation findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/geolocations/%s", uuid));
  }

  public GeoLocation findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/geolocations/identifier?namespace=%s&id=%s", namespace, id));
  }

  public GeoLocation save(GeoLocation geoLocation) throws HttpException {
    return doPostRequestForObject("/latest/geolocations", (GeoLocation) geoLocation);
  }

  public GeoLocation update(UUID uuid, GeoLocation geoLocation) throws HttpException {
    return doPutRequestForObject(String.format("/latest/geolocations/%s", uuid), (GeoLocation) geoLocation);
  }
}
