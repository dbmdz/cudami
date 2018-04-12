package de.digitalcollections.cudami.client.rest.impl;

import de.digitalcollections.cudami.client.rest.api.Client;
import de.digitalcollections.cudami.client.rest.backend.CudamiBackend;
import de.digitalcollections.cudami.client.rest.config.BackendUrls;
import feign.Feign;

public class ClientFactory {

  private final Feign.Builder feign;

  private final BackendUrls urls;

  public ClientFactory(Feign.Builder feign, BackendUrls urls) {
    this.feign = feign;
    this.urls = urls;
  }

  public Client createCudamiClient() {
    System.out.println("ClientFactory (CudamiClient): " + urls.forBackend("cudami"));
    CudamiBackend backend = feign.target(CudamiBackend.class, urls.forBackend("cudami"));

    return new CudamiClientImpl(backend);
  }
}
