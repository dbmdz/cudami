package de.digitalcollections.cudami.model.api.identifiable.entity;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A Website.
 */
public interface Website extends Entity {

  List<UUID> getRootPages();

  void setRootPages(List<UUID> rootPages);

  LocalDate getRegistrationDate();

  void setRegistrationDate(LocalDate registrationDate);

  URL getUrl();

  void setUrl(URL url);
}
