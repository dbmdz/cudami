package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * see {@link Website}
 */
public class WebsiteImpl extends EntityImpl implements Website<Long> {

  private LocalDate registrationDate;
  private List<ContentNode> rootNodes;
  private URL url;

  public WebsiteImpl() {
  }

  public WebsiteImpl(URL url) {
    this(null, url, null);
  }

  public WebsiteImpl(List<ContentNode> rootNodes, URL url, LocalDate registrationDate) {
    this.registrationDate = registrationDate;
    this.rootNodes = rootNodes;
    this.url = url;
  }

  @Override
  public EntityType getEntityType() {
    return EntityType.WEBSITE;
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
  public List<ContentNode> getRootNodes() {
    return rootNodes;
  }

  @Override
  public void setRootNodes(List<ContentNode> rootNodes) {
    this.rootNodes = rootNodes;
  }

}
