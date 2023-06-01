package de.digitalcollections.cudami.model.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.util.List;

@SuppressFBWarnings
public class IiifServerConfig {

  public static class Identifier {

    private List<String> namespaces;

    public List<String> getNamespaces() {
      return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
      this.namespaces = namespaces;
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
