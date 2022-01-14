package de.digitalcollections.cudami.template.website.sidebar.config;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "cudami")
@ConstructorBinding
public class CudamiConfig {

  private final Map<String, UUID> collections;
  private final Server server;
  private final Map<String, UUID> webpages;
  private final UUID website;

  public CudamiConfig(
      Map<String, UUID> collections, Server server, Map<String, UUID> webpages, UUID website) {
    this.collections = collections;
    this.server = server;
    this.webpages = webpages;
    this.website = website;
  }

  public UUID getCollection(String name) {
    return collections.get(name);
  }

  public Map<String, UUID> getCollections() {
    return collections;
  }

  public Server getServer() {
    return server;
  }

  public UUID getWebpage(String name) {
    return webpages.get(name);
  }

  public Map<String, UUID> getWebpages() {
    return webpages;
  }

  public UUID getWebsite() {
    return website;
  }

  public static class Server {

    private final URI url;

    public Server(URI url) {
      this.url = url;
    }

    public URI getUrl() {
      return url;
    }
  }
}
