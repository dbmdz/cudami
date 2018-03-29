package de.digitalcollections.cudami.model.impl.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * see {@link Website}
 */
public class WebsiteImpl extends EntityImpl implements Website {

  private LocalDate registrationDate;
  private List<UUID> rootPages = new ArrayList<>();
  private URL url;

  public WebsiteImpl() {
    super();
    this.entityType = EntityType.WEBSITE;
  }

  public WebsiteImpl(URL url) {
    this(null, url, null);
  }

  public WebsiteImpl(List<UUID> rootPages, URL url, LocalDate registrationDate) {
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
  public List<UUID> getRootPages() {
    return rootPages;
  }

  @Override
  public void setRootPages(List<UUID> rootPages) {
    this.rootPages = rootPages;
  }

}
