package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Website persistence handling.
 * @param <W> instance of website implementation */
public interface WebsiteRepository<W extends Website> extends EntityRepository<W> {

  List<Webpage> getRootPages(Website website);

  List<Webpage> getRootPages(UUID uuid);

  PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest);

  List<Locale> getLanguages();

  default boolean updateRootPagesOrder(W website, List<Webpage> rootPages) {
    if (website == null || rootPages == null) {
      return false;
    }
    return updateRootPagesOrder(website.getUuid(), rootPages);
  }

  boolean updateRootPagesOrder(UUID website, List<Webpage> rootPages);
}
