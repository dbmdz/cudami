package de.digitalcollections.cudami.server.business.api.service.identifiable.alias;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for UrlAliasses */
public interface UrlAliasService extends UniqueObjectService<UrlAlias> {

  /**
   * Delete all UrlAliases targetting the passed {@code Identifiable} except those that have already
   * been published.
   *
   * @param uuid the {@code targetUuid} whose UrlAliases should be deleted
   * @return true if at least one UrlAlias existed and could be deleted or false, if no UrlAlias
   *     existed at all and thus nothing could be deleted
   * @throws ServiceException
   */
  default boolean deleteAllForTarget(Identifiable targetIdentifiable) throws ServiceException {
    return deleteAllForTarget(targetIdentifiable, false);
  }

  /**
   * Delete all UrlAliases targetting the passed UUID except those that have already been published.
   *
   * @param uuid the {@code targetUuid} whose UrlAliases should be deleted
   * @param force if {@code true} remove published ones as well
   * @return true if at least one UrlAlias existed and could be deleted or false, if no UrlAlias
   *     existed at all and thus nothing could be deleted
   * @throws ServiceException
   */
  boolean deleteAllForTarget(Identifiable targetIdentifiable, boolean force)
      throws ServiceException;

  /**
   * Find UrlAliases
   *
   * @param pageRequest the PageRequest
   * @return a SearchPageResponse with the found LocalizedUrlAliases as paged content
   * @throws ServiceException in case of an error
   */
  PageResponse<LocalizedUrlAliases> findLocalizedUrlAliases(PageRequest pageRequest)
      throws ServiceException;

  /**
   * Generates a not yet existing slug for the provided label, language and websiteUuid. If the
   * websiteUuid is empty, the configured default website uuid is used.
   *
   * <p>If for the (locale,label,websiteUuid) triple a slug already exists, a new slug is calculated
   * by appending suffixes to it.
   *
   * @param pLocale The locale for which the slug is generated.
   * @param label The label as a string
   * @param websiteUuid The uuid of the website, for which the slug is generated. If not set, the
   *     UUID of the default website is used
   * @return slug as String, or null, if no website under the provided websiteUuid exists
   * @throws ServiceException
   */
  String generateSlug(Locale pLocale, String label, UUID websiteUuid) throws ServiceException;

  /**
   * Returns the LocalizedUrlAliases for an identifiable, identified by its UUID
   *
   * @param uuid the UUID of the identifiable
   * @return the LocalizedUrlAliases, if found, or null
   * @throws ServiceException in case of an error
   */
  LocalizedUrlAliases getLocalizedUrlAliases(UUID uuid) throws ServiceException;

  /**
   * Returns the primary Links (one per language) as LocalizedUrlAliases for a given slug for a
   * given website (identified by its uuid). The given website can be null. Additionally you can
   * provide a {@code Locale} language to retrieve primary links of this particular target language
   * only.
   *
   * @param websiteUuid the UUID of the website, the slug belongs to, or null
   * @param slug the slug (=relative path)
   * @param pLocale the locale for which the result is filtered. Optional.
   * @return LocalizedUrlAliases, if a primary link exists; otherwise: null.
   * @throws ServiceException in case of an error
   */
  LocalizedUrlAliases getPrimaryUrlAliases(UUID websiteUuid, String slug, Locale pLocale)
      throws ServiceException;

  /**
   * Returns all primary links of the passed target identifiable.
   *
   * @param targetUuid UUID of the identifiable that the primaries should be found for
   * @return {@code List}, not {@code null}
   * @throws ServiceException in case of an error
   */
  List<UrlAlias> getPrimaryUrlAliasesForTarget(UUID targetUuid) throws ServiceException;

  /**
   * Validates the given localizedUrlAliases according to the following criteria:
   *
   * <ul>
   *   <li>For each (website,target,language) tuple, exactly one primary UrlAlias must exist, and as
   *       many non primary UrlAliases can exist
   * </ul>
   *
   * @param localizedUrlAliases the LocalizedUrlAliases to validate
   * @throws ValidationException when the critera are not met
   */
  void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException;
}
