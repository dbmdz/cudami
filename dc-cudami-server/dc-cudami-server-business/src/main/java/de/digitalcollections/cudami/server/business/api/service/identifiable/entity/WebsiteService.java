package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  List<Locale> getLanguages();

  List<Webpage> getRootPages(Website website);

  List<Webpage> getRootPages(UUID uuid);

  PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest);

  boolean updateRootPagesOrder(Website website, List<Webpage> rootPages);
}
