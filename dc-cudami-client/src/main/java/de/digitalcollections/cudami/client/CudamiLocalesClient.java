package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

public class CudamiLocalesClient extends CudamiBaseClient<Locale> {

  public CudamiLocalesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Locale.class, mapper);
  }

  public List<String> findAllLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/languages", String.class);
  }

  public Locale getDefaultLanguage() throws HttpException {
    return doGetRequestForObject("/latest/languages/default");
  }

  @SuppressWarnings("unchecked")
  public List<String> findAllLocales() throws HttpException {
    return doGetRequestForObjectList("/latest/locales", String.class);
  }

  public String getDefaultLocale() throws HttpException {
    return doGetRequestForString("/latest/locales/default");
  }
}
