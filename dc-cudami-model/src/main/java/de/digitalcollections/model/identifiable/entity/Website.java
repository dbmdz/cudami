package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.identifiable.web.Webpage;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;

/** A Website. */
@SuperBuilder(buildMethodName = "prebuild")
public class Website extends Entity {

  private LocalDate registrationDate;
  private List<? extends Webpage> rootPages;
  private URL url;

  public Website() {
    super();
  }

  public Website(URL url) {
    this(null, url, null);
  }

  public Website(List<Webpage> rootPages, URL url, LocalDate registrationDate) {
    this();
    this.registrationDate = registrationDate;
    if (rootPages != null) this.rootPages = rootPages;
    this.url = url;
  }

  public LocalDate getRegistrationDate() {
    return registrationDate;
  }

  public List<? extends Webpage> getRootPages() {
    return rootPages;
  }

  public URL getUrl() {
    return url;
  }

  @Override
  protected void init() {
    super.init();
    if (rootPages == null) rootPages = new ArrayList<>(0);
  }

  public void setRegistrationDate(LocalDate registrationDate) {
    this.registrationDate = registrationDate;
  }

  public void setRootPages(List<? extends Webpage> rootPages) {
    this.rootPages = rootPages;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "Website{"
        + "registrationDate="
        + registrationDate
        + ", rootPages="
        + rootPages
        + ", url="
        + url
        + ", customAttributes="
        + customAttributes
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + ", notes="
        + notes
        + ", description="
        + description
        + ", identifiableObjectType="
        + identifiableObjectType
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", tags="
        + tags
        + ", type="
        + type
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + '}';
  }

  public abstract static class WebsiteBuilder<C extends Website, B extends WebsiteBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B registrationDate(String registrationDate) {
      this.registrationDate = LocalDate.parse(registrationDate);
      return self();
    }

    public B url(String url) {
      try {
        this.url = new URL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      return self();
    }
  }
}
