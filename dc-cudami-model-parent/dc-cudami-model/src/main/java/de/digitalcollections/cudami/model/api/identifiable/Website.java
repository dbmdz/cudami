package de.digitalcollections.cudami.model.api.identifiable;

import de.digitalcollections.cudami.model.api.Text;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * A Website.
 */
public interface Website extends Identifiable {

  Text getLabel();

  void setLabel(Text label);

  List<Node> getRootNodes();

  void setRootNodes(List<Node> rootNodes);

  LocalDate getRegistrationDate();

  void setRegistrationDate(LocalDate registrationDate);

  URL getUrl();

  void setUrl(URL url);
}
