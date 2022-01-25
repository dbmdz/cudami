package de.digitalcollections.cudami.client.legal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiLicensesClient extends CudamiBaseClient<License> {

  private static final String BASE_ENDPOINT_URL = "/v5/licenses";

  public CudamiLicensesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, License.class, mapper);
  }

  public License create() {
    return new License();
  }

  public PageResponse<License> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(BASE_ENDPOINT_URL, pageRequest);
  }

  public SearchPageResponse<License> find(SearchPageRequest pageRequest) throws HttpException {
    return this.doGetSearchRequestForPagedObjectList(BASE_ENDPOINT_URL, pageRequest);
  }

  public License findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format(BASE_ENDPOINT_URL + "/%s", uuid));
  }

  public License save(License license) throws HttpException {
    return doPostRequestForObject(BASE_ENDPOINT_URL, license);
  }

  public License update(UUID uuid, License license) throws HttpException {
    return doPutRequestForObject(String.format(BASE_ENDPOINT_URL + "/%s", uuid), license);
  }
}
