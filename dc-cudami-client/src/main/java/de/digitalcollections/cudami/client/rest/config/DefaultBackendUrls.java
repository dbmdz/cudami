package de.digitalcollections.cudami.client.rest.config;

public class DefaultBackendUrls implements BackendUrls {

  private final String url;

  public DefaultBackendUrls(String url) {
    this.url = url;
  }

  @Override
  public String forBackend(String backend) {
    return url;
  }

}
