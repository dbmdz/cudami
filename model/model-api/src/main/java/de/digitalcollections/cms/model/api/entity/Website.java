package de.digitalcollections.cms.model.api.entity;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * A Website.
 *
 * @param <ID> unique id specifying instance
 */
public interface Website<ID extends Serializable> extends Entity<ID> {

  URL getUrl();

  void setUrl(URL url);

  void setTitle(String title);

  String getTitle();
  
  List<ContentNode> getRootNodes();
  
  void setRootNodes(List<ContentNode> rootNodes);
}
