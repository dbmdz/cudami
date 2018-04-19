package de.digitalcollections.cudami.client.feign.impl;

import de.digitalcollections.cudami.client.feign.api.CudamiClient;
import de.digitalcollections.cudami.client.feign.backend.CudamiBackend;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.Locale;

public class CudamiClientImpl implements CudamiClient {

  private final CudamiBackend backend;

  public CudamiClientImpl(CudamiBackend backend) {
    this.backend = backend;
  }

  @Override
  public List<Locale> getAllLocales() throws Exception {
    return backend.getAllLocales();
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
