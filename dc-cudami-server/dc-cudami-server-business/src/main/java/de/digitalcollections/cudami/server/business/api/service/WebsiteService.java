package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;
import java.util.List;

/**
 * Service for Website.
 *
 * @param <W> domain object
 * @param <ID> unique id
 */
public interface WebsiteService<W extends Website, ID extends Serializable> extends EntityService<W, ID> {

  List<ContentNode> getRootCategories(W website);
}
