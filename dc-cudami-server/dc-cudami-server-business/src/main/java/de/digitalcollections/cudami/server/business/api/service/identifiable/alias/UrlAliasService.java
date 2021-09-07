package de.digitalcollections.cudami.server.business.api.service.identifiable.alias;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for UrlAliasses */
public interface UrlAliasService {

  /**
   * Retrieve one UrlAlias by its UUID
   *
   * @param uuid the UUID
   * @return the UrlAlias or null
   * @throws CudamiServiceException in case of an error
   */
  UrlAlias findOne(UUID uuid) throws CudamiServiceException;

  /**
   * Delete a single UrlAlias by its UUID
   *
   * @param uuid the UUID
   * @return true if the UrlAliases existed and could be deleted or false, it it did not exist and
   *     thus could not be deleted
   * @throws CudamiServiceException in case of an error
   */
  default boolean delete(UUID uuid) throws CudamiServiceException {
    return delete(List.of(uuid));
  }

  /**
   * Delete a list of UrlAliases by their UUIDs
   *
   * @param uuids a List of UUIDs
   * @return true if at least one UrlAlias existed and could be deleted of false, if no UrlAlias
   *     existed at all and thus nothing could be deleted
   * @throws CudamiServiceException
   */
  boolean delete(List<UUID> uuids) throws CudamiServiceException;

  /**
   * Delete all UrlAliases targetting the passed UUID.
   *
   * @param uuid the {@code targetUuid} whose UrlAliases should be deleted
   * @return true if at least one UrlAlias existed and could be deleted of false, if no UrlAlias
   *     existed at all and thus nothing could be deleted
   * @throws CudamiServiceException
   */
  boolean deleteAllForTarget(UUID uuid) throws CudamiServiceException;

  /**
   * Create an UrlAlias in the database
   *
   * @param urlAlias the UrlAlias (with yet empty UUID)
   * @return the persisted UrlAlias with its generated UUID
   * @throws CudamiServiceException
   */
  UrlAlias create(UrlAlias urlAlias) throws CudamiServiceException;

  /**
   * Updates an UrlAlias in the database
   *
   * @param urlAlias the UrlAlias (with set UUID)
   * @return the updated UrlAlias
   * @throws CudamiServiceException
   */
  UrlAlias update(UrlAlias urlAlias) throws CudamiServiceException;

  /**
   * Find UrlAliases
   *
   * @param pageRequest the PageRequest
   * @return a SearchPageResponse with the found LocalizedUrlAliases as paged content
   */
  SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest pageRequest)
      throws CudamiServiceException;

  /**
   * Returns the LocalizedUrlAliases for an identifiable, identified by its UUID
   *
   * @param uuid the UUID of the identifiable
   * @return the LocalizedUrlAliases, if found, or null
   * @throws CudamiServiceException in case of an error
   */
  LocalizedUrlAliases findLocalizedUrlAliases(UUID uuid) throws CudamiServiceException;

  /**
   * Returns the primary Links (one per language) as LocalizedUrlAliases for a given slug for a
   * given website (identified by its uuid). The given website can be null.
   *
   * @param websiteUuid the UUID of the website, the slug belongs to, or null
   * @param slug the slug (=relative path)
   * @return LocalizedUrlAliases, if a primary link exists; otherwise: null.
   * @throws CudamiServiceException in case of an error
   */
  LocalizedUrlAliases findPrimaryLinks(UUID websiteUuid, String slug) throws CudamiServiceException;

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
   * @throws CudamiServiceException
   */
  String generateSlug(Locale pLocale, String label, UUID websiteUuid) throws CudamiServiceException;
}
