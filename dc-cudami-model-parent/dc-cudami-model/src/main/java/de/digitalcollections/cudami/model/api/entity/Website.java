package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * A Website.
 */
public interface Website extends Entity {

  List<Node> getRootNodes();

  void setRootNodes(List<Node> rootNodes);

  LocalDate getRegistrationDate();

  void setRegistrationDate(LocalDate registrationDate);

  URL getUrl();

  void setUrl(URL url);
}
