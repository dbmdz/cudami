package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;

public interface IdentifiableService<I extends Identifiable> extends UniqueObjectService<I> {

  void addRelatedEntity(I identifiable, Entity entity) throws ServiceException;

  void addRelatedFileresource(I identifiable, FileResource fileResource) throws ServiceException;

  default void cleanupLabelFromUnwantedLocales(
      Locale locale, Locale fallbackLocale, LocalizedText label) {
    // If no locales exist at all, we cannot do anything useful here
    if (label == null || label.getLocales() == null || label.getLocales().isEmpty()) {
      return;
    }

    // Prepare the fallback solutions, when no label for the desired locale exists.
    // Retrieve the value for the fallback locale and bypass a "feature" of the
    // LocalizedText class, which would return the "first" value, if no value for
    // the
    // given locale exists. This is NOT what we want here!
    String defaultLabel = null;
    if (label.getLocales().contains(fallbackLocale)) {
      defaultLabel = label.getText(fallbackLocale);
    }

    Locale firstLocale = label.getLocales().get(0);
    String firstLocaleLabel = label.getText(firstLocale);

    // Remove all locale/text pairs, which don't apply to the demanded language
    // but ensure, that in the end, if nothing is left, one of the fallbacks are
    // applied.
    label.entrySet().removeIf(e -> e.getKey() != locale);
    if (label.keySet().isEmpty()) {
      // No entry for the desired language found!
      if (defaultLabel != null) {
        // The entry for the "default" language exists. We use it.
        label.put(fallbackLocale, defaultLabel);
      } else if (firstLocale != null) {
        // Pick the first locale and its text (if it exists)
        label.put(firstLocale, firstLocaleLabel);
      }
    }
  }

  // TODO: replace with filtering
  PageResponse<I> findByLanguageAndInitial(PageRequest pageRequest, String language, String initial)
      throws ServiceException;

  PageResponse<Entity> findRelatedEntities(I identifiable, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<FileResource> findRelatedFileResources(I identifiable, PageRequest pageRequest)
      throws ServiceException;

  I getByExampleAndLocale(I identifiable, Locale locale) throws ServiceException;

  I getByIdentifier(Identifier identifier) throws ServiceException;

  List<Locale> getLanguages() throws ServiceException;

  List<Entity> setRelatedEntities(I identifiable, List<Entity> entities) throws ServiceException;

  List<FileResource> setRelatedFileResources(I identifiable, List<FileResource> fileResources)
      throws ServiceException;

  void validate(I identifiable) throws ServiceException, ValidationException;
}
