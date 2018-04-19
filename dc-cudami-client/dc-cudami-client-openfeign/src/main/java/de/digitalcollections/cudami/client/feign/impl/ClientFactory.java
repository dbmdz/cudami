package de.digitalcollections.cudami.client.feign.impl;

import de.digitalcollections.cudami.client.feign.api.Client;
import de.digitalcollections.cudami.client.feign.backend.CudamiBackend;
import de.digitalcollections.cudami.client.feign.config.BackendUrls;
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
