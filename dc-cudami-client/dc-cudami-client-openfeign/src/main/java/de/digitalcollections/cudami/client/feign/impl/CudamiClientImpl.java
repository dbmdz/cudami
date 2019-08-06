package de.digitalcollections.cudami.client.feign.impl;

import de.digitalcollections.cudami.client.feign.api.CudamiClient;
import de.digitalcollections.cudami.client.feign.backend.CudamiBackend;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.Locale;

public class CudamiClientImpl implements CudamiClient {

  private final CudamiBackend backend;

  public CudamiClientImpl(CudamiBackend backend) {
    this.backend = backend;
  }

  @Override
  public List<String> getSupportedLanguages() throws Exception {
    return backend.getSupportedLanguages();
  }

  @Override
  public String getDefaultLanguage() throws Exception {
    return backend.getDefaultLanguage();
  }

  @Override
  public List<Locale> getSupportedLocales() throws Exception {
    return backend.getSupportedLocales();
  }

  @Override
  public Locale getDefaultLocale() throws Exception {
    return backend.getDefaultLocale();
  }

  @Override
  public Webpage getWebpage(String uuid) {
    return backend.getWebpage(uuid);
  }

  @Override
  public Webpage getWebpage(Locale locale, String uuid) {
    return backend.getWebpage(locale, uuid);
  }

  @Override
  public Website getWebsite(String uuid) {
    return backend.getWebsite(uuid);
  }
}
