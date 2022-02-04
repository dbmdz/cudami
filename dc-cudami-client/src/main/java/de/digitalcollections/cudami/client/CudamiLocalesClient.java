package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.http.HttpException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiLocalesClient extends BaseRestClient<Locale> {

  public CudamiLocalesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Locale.class, mapper, "/v5/locales");
  }

  public List<String> findAllLanguages() throws HttpException {
    return doGetRequestForObjectList("/v5/languages", String.class);
  }

  @SuppressWarnings("unchecked")
  public List<String> findAllLocales() throws HttpException {
    return doGetRequestForObjectList(baseEndpoint, String.class);
  }

  public Locale getDefaultLanguage() throws HttpException {
    return doGetRequestForObject("/v5/languages/default");
  }

  public String getDefaultLocale() throws HttpException {
    return doGetRequestForString(baseEndpoint + "/default");
  }
}
