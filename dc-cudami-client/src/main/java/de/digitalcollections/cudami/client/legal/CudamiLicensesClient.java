package de.digitalcollections.cudami.client.legal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class CudamiLicensesClient extends CudamiRestClient<License> {

  public CudamiLicensesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, License.class, mapper, "/v5/licenses");
  }

  /**
   * @param url (not validated) url string of license to be deleted
   * @throws java.net.MalformedURLException thrown if given url is malformed
   * @throws TechnicalException in case of connection problems
   */
  public void deleteByUrl(String url) throws TechnicalException, MalformedURLException {
    deleteByUrl(URI.create(url).toURL());
  }

  /**
   * @param url valid url of license to be deleted
   * @throws TechnicalException in case of connection problems
   */
  public void deleteByUrl(URL url) throws TechnicalException {
    doDeleteRequestForString(
        baseEndpoint + "?url=" + URLEncoder.encode(url.toString(), StandardCharsets.UTF_8));
  }

  @Deprecated
  public void deleteByUuids(List<UUID> uuids) {
    throw new UnsupportedOperationException(
        "Spec https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html not explicitely allows a body in DELETE methods. Java HTTP RequestBuilder does not allow.");
  }

  public License getByUrl(String url) throws TechnicalException {
    return doGetRequestForObject(
        String.format(baseEndpoint + "?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8)));
  }

  public License getByUrl(URL url) throws TechnicalException {
    return getByUrl(url.toString());
  }
}
