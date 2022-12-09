package de.digitalcollections.cudami.client;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CudamiLocalesClient extends BaseRestClient<Locale> {

  public CudamiLocalesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Locale.class, mapper, API_VERSION_PREFIX + "/locales");
  }

  public List<String> getAllLanguages() throws TechnicalException {
    return doGetRequestForObjectList(API_VERSION_PREFIX + "/languages", String.class);
  }

  public List<Locale> getAllLanguagesAsLocales() throws TechnicalException {
    List<Locale> allLocales = getAllLocales();
    return allLocales.stream().filter(l -> l.getCountry().isBlank()).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public List<Locale> getAllLocales() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint, Locale.class);
  }

  public Locale getDefaultLanguage() throws TechnicalException {
    try {
      return doGetRequestForObject(API_VERSION_PREFIX + "/languages/default");
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public String getDefaultLocale() throws TechnicalException {
    try {
      return doGetRequestForString(baseEndpoint + "/default");
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }
}
