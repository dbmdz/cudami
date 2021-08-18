package de.digitalcollections.cudami.server.backend.api.repository.alias;

import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import java.util.List;
import java.util.UUID;

public interface UrlAliasRepository {

  /**
   * Remove the entries with the provided UUIDs (PK).
   *
   * @param urlAliasUuids List of aliases' UUIDs to remove
   * @return count of removed datasets
   */
  int delete(List<UUID> urlAliasUuids);

  /**
   * Retrieve all slugs of a link target.
   *
   * @param uuid the target's (Webpage, Collection,...) UUID
   * @return {@code LocalizedUrlAliases} containing all {@code UrlAlias} objects for that target
   */
  LocalizedUrlAliases findAllForTarget(UUID uuid);

  /**
   * Retrieve the main link of a link target.
   *
   * @param uuid the target's (Webpage, Collection,...) UUID
   * @return the {@code UrlAlias} object with {@code isMainAlias() == true}
   */
  UrlAlias findMainLink(UUID uuid);

  /** Retrieve the {@code UrlAlias} with the supplied UUID (PK). */
  UrlAlias findOne(UUID uuid);

  /**
   * Save an {@code UrlAlias} object.
   *
   * @param urlAlias the object to save
   * @return the newly created dataset
   */
  UrlAlias save(UrlAlias urlAlias);

  /**
   * Update an existing object.
   *
   * @param urlAlias the existing object with changed properties
   * @return the updated dataset
   */
  UrlAlias update(UrlAlias urlAlias);
}
