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
import java.util.Locale;

public class CudamiLicensesClient extends CudamiRestClient<License> {

  public CudamiLicensesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, License.class, mapper, API_VERSION_PREFIX + "/licenses");
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
        String.format(
            "%s?url=%s", baseEndpoint, URLEncoder.encode(url.toString(), StandardCharsets.UTF_8)));
  }

  public License getByUrl(String url) throws TechnicalException {
    return doGetRequestForObject(
        String.format("%s?url=%s", baseEndpoint, URLEncoder.encode(url, StandardCharsets.UTF_8)));
  }

  public License getByUrl(URL url) throws TechnicalException {
    return getByUrl(url.toString());
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return this.doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }
}
