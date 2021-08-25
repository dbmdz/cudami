package de.digitalcollections.cudami.server.backend.api.repository.alias;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.UUID;

public interface UrlAliasRepository {

  /**
   * Remove the entries with the provided UUIDs (PK).
   *
   * @param urlAliasUuids List of aliases' UUIDs to remove
   * @return count of removed datasets
   * @throws UrlAliasRepositoryException
   */
  int delete(List<UUID> urlAliasUuids) throws UrlAliasRepositoryException;

  /** Generic search method. */
  SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws UrlAliasRepositoryException;

  /**
   * Retrieve all slugs of a link target.
   *
   * @param uuid the target's (Webpage, Collection,...) UUID
   * @return {@code LocalizedUrlAliases} containing all {@code UrlAlias} objects for that target
   * @throws UrlAliasRepositoryException
   */
  LocalizedUrlAliases findAllForTarget(UUID uuid) throws UrlAliasRepositoryException;

  /**
   * Retrieve the main links corresponding to a slug.
   *
   * @param websiteUuid the owning website's UUID
   * @param slug the slug to retrieve the main alias for
   * @return all {@code UrlAlias}es with {@code isMainAlias() == true}
   * @throws UrlAliasRepositoryException
   */
  LocalizedUrlAliases findMainLinks(UUID websiteUuid, String slug)
      throws UrlAliasRepositoryException;

  /**
   * Retrieve the {@code UrlAlias} with the supplied UUID (PK).
   *
   * @return the found {@code UrlAlias} or {@code null}
   * @throws UrlAliasRepositoryException
   */
  UrlAlias findOne(UUID uuid) throws UrlAliasRepositoryException;

  /** Check whether an entry exists for the passed website UUID and slug. */
  boolean hasUrlAlias(UUID websiteUuid, String slug) throws UrlAliasRepositoryException;

  /**
   * Save an {@code UrlAlias} object.
   *
   * @param urlAlias the object to save
   * @return the newly created dataset or {@code null}
   * @throws UrlAliasRepositoryException
   */
  UrlAlias save(UrlAlias urlAlias) throws UrlAliasRepositoryException;

  /**
   * Update an existing object.
   *
   * @param urlAlias the existing object with changed properties
   * @return the updated dataset or {@code null}
   * @throws UrlAliasRepositoryException
   */
  UrlAlias update(UrlAlias urlAlias) throws UrlAliasRepositoryException;
}
