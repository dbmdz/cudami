package de.digitalcollections.cudami.model.api.entity;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * A Website.
 *
 * @param <ID> unique id specifying instance
 */
public interface Website<ID extends Serializable> extends Entity<ID> {

  List<ContentNode> getRootNodes();

  void setRootNodes(List<ContentNode> rootNodes);

  LocalDate getRegistrationDate();

  void setRegistrationDate(LocalDate registrationDate);

  URL getUrl();

  void setUrl(URL url);
}
