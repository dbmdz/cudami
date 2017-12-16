package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * see {@link Website}
 */
public class WebsiteImpl extends EntityImpl implements Website {

  private LocalDate registrationDate;
  private List<Node> rootNodes;
  private URL url;

  public WebsiteImpl() {
    this.entityType = EntityType.WEBSITE;
  }

  public WebsiteImpl(URL url) {
    this(null, url, null);
  }

  public WebsiteImpl(List<Node> rootNodes, URL url, LocalDate registrationDate) {
    this();
    this.registrationDate = registrationDate;
    this.rootNodes = rootNodes;
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
  public List<Node> getRootNodes() {
    return rootNodes;
  }

  @Override
  public void setRootNodes(List<Node> rootNodes) {
    this.rootNodes = rootNodes;
  }

}
