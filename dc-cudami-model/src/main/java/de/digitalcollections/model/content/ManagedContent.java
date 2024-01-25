package de.digitalcollections.model.content;

import java.time.LocalDate;

/**
 * Content object that is managed by a content management system and has management related
 * properties.
 */
public interface ManagedContent {

  public LocalDate getPublicationEnd();

  public LocalDate getPublicationStart();

  public PublicationStatus getPublicationStatus();

  public void setPublicationEnd(LocalDate publicationEnd);

  public void setPublicationStart(LocalDate publicationStart);

  public void setPublicationStatus(PublicationStatus publicationStatus);
}
