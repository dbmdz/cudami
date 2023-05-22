package de.digitalcollections.cudami.server.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iiif")
@SuppressFBWarnings
public class IiifServerConfig {

  public static class Identifier {

    private String namespace;

    public Identifier(String namespace) {
      this.namespace = namespace;
    }

    public String getNamespace() {
      return namespace;
    }

    public void setNamespace(String namespace) {
      this.namespace = namespace;
    }
  }

  public static class Image {

    private URI baseUrl;

    public Image(URI baseUrl) {
      this.baseUrl = baseUrl;
    }

    public URI getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(URI baseUri) {
      this.baseUrl = baseUri;
    }
  }

  public static class Presentation {

    private URI baseUrl;

    public Presentation(URI baseUrl) {
      this.baseUrl = baseUrl;
    }

    public URI getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(URI baseUri) {
      this.baseUrl = baseUri;
    }
  }

  private final Identifier identifier;

  private final Image image;

  private final Presentation presentation;

  public IiifServerConfig(Identifier identifier, Image image, Presentation presentation) {
    this.identifier = identifier;
    this.image = image;
    this.presentation = presentation;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public Image getImage() {
    return image;
  }

  public Presentation getPresentation() {
    return presentation;
  }
}
