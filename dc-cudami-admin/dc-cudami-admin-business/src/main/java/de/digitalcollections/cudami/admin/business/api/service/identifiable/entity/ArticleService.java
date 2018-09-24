package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for Article.
 *
 * @param <A> domain object
 */
public interface ArticleService<A extends Article> extends EntityService<A>, NodeService<A>, IdentifiablesContainerService<A> {

  A get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  A saveWithParent(A article, UUID parentUuid) throws IdentifiableServiceException;
}
