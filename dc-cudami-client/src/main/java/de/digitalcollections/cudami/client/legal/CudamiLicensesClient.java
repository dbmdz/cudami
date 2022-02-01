package de.digitalcollections.cudami.client.legal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class CudamiLicensesClient extends CudamiBaseClient<License> {

  private static final String BASE_ENDPOINT_URL = "/v5/licenses";

  public CudamiLicensesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, License.class, mapper);
  }

  public long count() throws HttpException {
    String result = doGetRequestForString(BASE_ENDPOINT_URL + "/count");
    return Long.parseLong(result);
  }

  public License create() {
    return new License();
  }

  /**
   * @param url (not validated) url string of license to be deleted
   * @throws de.digitalcollections.cudami.client.exceptions.HttpException in case of connection
   *     problems
   */
  public void deleteByUrl(String url) throws HttpException, MalformedURLException {
    deleteByUrl(URI.create(url).toURL());
  }

  /**
   * @param url valid url of license to be deleted
   * @throws de.digitalcollections.cudami.client.exceptions.HttpException in case of connection
   *     problems
   */
  public void deleteByUrl(URL url) throws HttpException {
    doDeleteRequestForString(
        BASE_ENDPOINT_URL + "?url=" + URLEncoder.encode(url.toString(), StandardCharsets.UTF_8));
  }

  public void deleteByUuid(UUID uuid) throws HttpException {
    doDeleteRequestForString(String.format(BASE_ENDPOINT_URL + "/%s", uuid));
  }

  @Deprecated
  public void deleteByUuids(List<UUID> uuids) {
    throw new UnsupportedOperationException(
        "Spec https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html not explicitely allows a body in DELETE methods. Java HTTP RequestBuilder does not allow.");
  }

  public PageResponse<License> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(BASE_ENDPOINT_URL, pageRequest);
  }

  public List<License> findAll() throws HttpException {
    return doGetRequestForObjectList(BASE_ENDPOINT_URL + "/all");
  }

  public License getByUrl(String url) throws HttpException {
    return doGetRequestForObject(
        String.format(
            BASE_ENDPOINT_URL + "?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8)));
  }

  public License getByUrl(URL url) throws HttpException {
    return getByUrl(url.toString());
  }

  public License getByUuid(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format(BASE_ENDPOINT_URL + "/%s", uuid));
  }

  public License save(License license) throws HttpException {
    return doPostRequestForObject(BASE_ENDPOINT_URL, license);
  }

  public License update(UUID uuid, License license) throws HttpException {
    return doPutRequestForObject(String.format(BASE_ENDPOINT_URL + "/%s", uuid), license);
  }
}
