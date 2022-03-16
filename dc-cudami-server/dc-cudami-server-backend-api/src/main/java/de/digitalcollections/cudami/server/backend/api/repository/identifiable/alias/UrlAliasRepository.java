package de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
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
   * Retrieve all primary links corresponding to a slug. The owning website is ignored.
   *
   * @param slug the slug to retrieve the primary aliases for
   * @return all {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws UrlAliasRepositoryException
   */
  LocalizedUrlAliases findAllPrimaryLinks(String slug) throws UrlAliasRepositoryException;

  /**
   * Retrieve the primary links corresponding to a slug and the target's language.
   *
   * @param websiteUuid the owning website's UUID, can be {@code null} and will be selected as is
   * @param slug the slug to retrieve the primary aliases for
   * @return {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws UrlAliasRepositoryException
   */
  default LocalizedUrlAliases findPrimaryLinksForWebsite(UUID websiteUuid, String slug)
      throws UrlAliasRepositoryException {
    return findPrimaryLinksForWebsite(websiteUuid, slug, true);
  }

  /**
   * Retrieve the primary links corresponding to a slug.
   *
   * @param websiteUuid the owning website's UUID, can be {@code null} and will be selected as is
   * @param slug the slug to retrieve the primary aliases for
   * @param considerLanguage if true consider the language(s) of the target when searching for the
   *     primary links
   * @return {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws UrlAliasRepositoryException
   */
  LocalizedUrlAliases findPrimaryLinksForWebsite(
      UUID websiteUuid, String slug, boolean considerLanguage) throws UrlAliasRepositoryException;

  /**
   * Retrieve the {@code UrlAlias} with the supplied UUID (PK).
   *
   * @return the found {@code UrlAlias} or {@code null}
   * @throws UrlAliasRepositoryException
   */
  UrlAlias getByUuid(UUID uuid) throws UrlAliasRepositoryException;

  /**
   * Check whether an entry exists for the passed website UUID, slug and language.
   *
   * @param slug not null
   * @param websiteUuid can be null
   * @param targetLanguage can be null
   */
  boolean hasUrlAlias(String slug, UUID websiteUuid, Locale targetLanguage)
      throws UrlAliasRepositoryException;

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
