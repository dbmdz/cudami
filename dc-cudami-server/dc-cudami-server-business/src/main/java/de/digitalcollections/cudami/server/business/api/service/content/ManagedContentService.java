package de.digitalcollections.cudami.server.business.api.service.content;

import de.digitalcollections.model.content.ManagedContent;
import de.digitalcollections.model.content.PublicationStatus;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.time.TimeBasedStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for ManagedContent related business logic.
 *
 * @param <T> content object that is managed
 */
public interface ManagedContentService<T extends ManagedContent> {

  static Filtering filteringForActive() {
    // business logic that defines, what "active" means
    LocalDate now = LocalDate.now();
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("publicationStart")
                    .lessOrEqualAndSet(now)
                    .build())
            .add(
                FilterCriterion.builder()
                    .withExpression("publicationEnd")
                    .greaterOrNotSet(now)
                    .build())
            .build();

    return filtering;
  }

  default void setPublicationStatus(List<T> managedContentObjects) {
    if (managedContentObjects == null || managedContentObjects.isEmpty()) {
      return;
    }
    managedContentObjects.stream().forEach(mco -> setPublicationStatus(mco));
  }

  default void setPublicationStatus(T managedContentObject) {
    if (managedContentObject == null) {
      return;
    }
    TimeBasedStatus tbs =
        TimeBasedStatus.get(
            new LocalDateRange(
                managedContentObject.getPublicationStart(),
                managedContentObject.getPublicationEnd()),
            LocalDate.now());
    switch (tbs) {
      case NOT_YET_IN_RANGE ->
          managedContentObject.setPublicationStatus(PublicationStatus.NOT_YET_PUBLISHED);
      case IS_IN_RANGE -> managedContentObject.setPublicationStatus(PublicationStatus.PUBLISHED);
      case NO_LONGER_IN_RANGE ->
          managedContentObject.setPublicationStatus(PublicationStatus.NO_LONGER_PUBLISHED);
    }
  }
}
