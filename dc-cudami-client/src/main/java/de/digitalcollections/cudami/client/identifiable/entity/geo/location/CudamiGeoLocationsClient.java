package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiGeoLocationsClient extends CudamiBaseClient<GeoLocation> {

  public CudamiGeoLocationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GeoLocation.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/geolocations/count"));
  }

  public GeoLocation create() {
    return new GeoLocation();
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<GeoLocation> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/geolocations", pageRequest);
  }

  public SearchPageResponse<GeoLocation> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/geolocations", searchPageRequest);
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

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/geolocations/languages", Locale.class);
  }

  public GeoLocation save(GeoLocation geoLocation) throws HttpException {
    return doPostRequestForObject("/latest/geolocations", geoLocation);
  }

  public GeoLocation update(UUID uuid, GeoLocation geoLocation) throws HttpException {
    return doPutRequestForObject(String.format("/latest/geolocations/%s", uuid), geoLocation);
  }
}
