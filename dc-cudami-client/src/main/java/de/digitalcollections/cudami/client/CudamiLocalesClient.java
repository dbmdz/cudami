package de.digitalcollections.cudami.client;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiLocalesClient extends BaseRestClient<Locale> {

  public CudamiLocalesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Locale.class, mapper, API_VERSION_PREFIX + "/locales");
  }

  public List<String> getAllLanguages() throws TechnicalException {
    return doGetRequestForObjectList(API_VERSION_PREFIX + "/languages", String.class);
  }

  @SuppressWarnings("unchecked")
  public List<String> getAllLocales() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint, String.class);
  }

  public Locale getDefaultLanguage() throws TechnicalException {
    return doGetRequestForObject(API_VERSION_PREFIX + "/languages/default");
  }

  public String getDefaultLocale() throws TechnicalException {
    return doGetRequestForString(baseEndpoint + "/default");
  }
}
