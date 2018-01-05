package de.digitalcollections.cudami.model.api.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * A Website.
 */
public interface Website extends Entity {

  List<Webpage> getRootPages();

  void setRootPages(List<Webpage> rootPages);

  LocalDate getRegistrationDate();

  void setRegistrationDate(LocalDate registrationDate);

  URL getUrl();

  void setUrl(URL url);
}
