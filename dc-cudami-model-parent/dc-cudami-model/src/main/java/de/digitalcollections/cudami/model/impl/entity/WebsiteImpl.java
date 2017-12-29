package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.Webpage;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * see {@link Website}
 */
public class WebsiteImpl extends EntityImpl implements Website {

  private LocalDate registrationDate;
  private List<Webpage> rootPages;
  private URL url;

  public WebsiteImpl() {
    this.entityType = EntityType.WEBSITE;
  }

  public WebsiteImpl(URL url) {
    this(null, url, null);
  }

  public WebsiteImpl(List<Webpage> rootPages, URL url, LocalDate registrationDate) {
    this();
    this.registrationDate = registrationDate;
    this.rootPages = rootPages;
    this.url = url;
  }

  @Override
  public LocalDate getRegistrationDate() {
    return registrationDate;
  }

  @Override
  public void setRegistrationDate(LocalDate registrationDate) {
    this.registrationDate = registrationDate;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public List<Webpage> getRootPages() {
    return rootPages;
  }

  @Override
  public void setRootPages(List<Webpage> rootPages) {
    this.rootPages = rootPages;
  }

}
