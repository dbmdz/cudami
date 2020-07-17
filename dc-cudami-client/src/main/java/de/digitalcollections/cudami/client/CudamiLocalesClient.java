package de.digitalcollections.cudami.client;

import java.util.List;
import java.util.Locale;

public class CudamiLocalesClient extends CudamiBaseClient<Locale> {

  public CudamiLocalesClient(String serverUrl) {
    super(serverUrl, Locale.class);
  }

  public List<String> findAllLanguages() throws Exception {
    return doGetRequestForObjectList("/latest/languages", String.class);
  }

  public Locale getDefaultLanguage() throws Exception {
    return doGetRequestForObject("/latest/languages/default");
  }

  @SuppressWarnings("unchecked")
  public List<String> findAllLocales() throws Exception {
    return doGetRequestForObjectList("/latest/locales", String.class);
  }

  public String getDefaultLocale() throws Exception {
    return doGetRequestForString("/latest/locales/default");
  }
}
