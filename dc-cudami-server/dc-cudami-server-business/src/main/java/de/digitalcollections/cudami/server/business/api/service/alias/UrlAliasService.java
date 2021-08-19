package de.digitalcollections.cudami.server.business.api.service.alias;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
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
   * Returns the main Link as LocalizedUrlAliases for a given slug for a given website (identified
   * by its uuid)
   *
   * @param websiteUuid the UUID of the website, the slug belongs to
   * @param slug the slug (=relative path)
   * @return LocalizedUrlAliases, if a main link exists; otherwise: null.
   * @throws CudamiServiceException in case of an error
   */
  LocalizedUrlAliases findMainLink(UUID websiteUuid, String slug) throws CudamiServiceException;
}
