package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IdentifiableService<I extends Identifiable> {

  default void cleanupLabelFromUnwantedLocales(Locale locale, Locale fallbackLocale, Node n) {
    LocalizedText label = n.getLabel();

    // If no locales exist at all, we cannot do anything useful here
    if (label == null || label.getLocales() == null || label.getLocales().isEmpty()) {
      return;
    }

    // Prepare the fallback solutions, when no label for the desired locale exists.
    // Retrieve the value for the fallback locale and bypass a "feature" of the
    // LocalizedText class, which would return the "first" value, if no value for the
    // given locale exists. This is NOT what we want here!
    String defaultLabel = null;
    if (label.getLocales().contains(fallbackLocale)) {
      defaultLabel = label.getText(fallbackLocale);
    }

    Locale firstLocale = label.getLocales().get(0);
    String firstLocaleLabel = label.getText(firstLocale);

    // Remove all locale/text pairs, which don't apply to the demanded language
    // but ensure, that in the end, if nothing is left, one of the fallbacks are applied.
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

  long count();

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  boolean delete(List<UUID> uuids);

  PageResponse<I> find(PageRequest pageRequest);

  SearchPageResponse<I> find(SearchPageRequest searchPageRequest);

  List<I> find(String searchTerm, int maxResults);

  /**
   * @return list of ALL identifiables with FULL data. USE WITH CARE (only for internal workflow,
   *     NOT FOR USER INTERACTION!)!!!
   */
  List<I> findAllFull();

  /**
   * Returns a list of all identifiables, reduced to their identifiers and last modification date
   *
   * @return partially filled complete list of all identifiables of implementing repository entity
   *     type
   */
  List<I> findAllReduced();

  PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  I get(Identifier identifier);

  I get(UUID uuid);

  I get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  I getByIdentifier(String namespace, String id);

  I save(I identifiable) throws IdentifiableServiceException;

  I update(I identifiable) throws IdentifiableServiceException;
}
