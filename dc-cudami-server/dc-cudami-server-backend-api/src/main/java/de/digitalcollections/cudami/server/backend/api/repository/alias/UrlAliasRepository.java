package de.digitalcollections.cudami.server.backend.api.repository.alias;

import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import java.util.List;
import java.util.UUID;

public interface UrlAliasRepository {

  int delete(List<UUID> urlAliasUuids);

  LocalizedUrlAliases findAllForTarget(UUID uuid);

  UrlAlias findMainLink(UUID uuid);

  UrlAlias findOne(UUID uuid);

  UrlAlias save(UrlAlias urlAlias);

  UrlAlias update(UrlAlias urlAlias);
}
