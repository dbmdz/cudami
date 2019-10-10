package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.UUID;

/** Repository for Website persistence handling. */
public interface WebsiteRepository extends EntityRepository<Website> {

  List<Webpage> getRootPages(Website website);

  List<Webpage> getRootPages(UUID uuid);
}
