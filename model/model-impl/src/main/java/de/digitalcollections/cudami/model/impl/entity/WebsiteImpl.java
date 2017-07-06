package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class WebsiteImpl<ID extends Serializable> extends EntityImpl<ID> implements Website<ID> {

  private List<ContentNode> rootNodes;
  private String title;
  private URL url;

  public WebsiteImpl() {
  }

  public WebsiteImpl(List<ContentNode> rootNodes, String title, URL url) {
    this.rootNodes = rootNodes;
    this.title = title;
    this.url = url;
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
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
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
