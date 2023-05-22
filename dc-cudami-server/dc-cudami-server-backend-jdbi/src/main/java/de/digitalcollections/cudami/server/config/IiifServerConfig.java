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

    public String getNamespace() {
      return namespace;
    }

    public void setNamespace(String namespace) {
      this.namespace = namespace;
    }
  }

  public static class Image {

    private URI baseUrl;

    public URI getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  public static class Presentation {

    private URI baseUrl;

    public URI getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  private Identifier identifier;

  private Image image;

  private Presentation presentation;

  public Identifier getIdentifier() {
    return identifier;
  }

  public Image getImage() {
    return image;
  }

  public Presentation getPresentation() {
    return presentation;
  }

  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public void setPresentation(Presentation presentation) {
    this.presentation = presentation;
  }
}
