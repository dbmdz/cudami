package de.digitalcollections.cudami.frontend.website.config;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "cudami")
@ConstructorBinding
public class CudamiConfig {

  private final Server server;
  private final Map<String, UUID> webpages;
  private final UUID website;

  public CudamiConfig(Server server, Map<String, UUID> webpages, UUID website) {
    this.server = server;
    this.webpages = webpages;
    this.website = website;
  }

  public Server getServer() {
    return server;
  }

  public UUID getWebpage(String name) {
    if (webpages == null) {
      return null;
    }
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
