package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for Article.
 *
 * @param <A> article instance
 * @param <I> identifiable instance
 */
public interface ArticleService<A extends Article, I extends Identifiable> extends EntityService<A>, IdentifiablesContainerService<A, I> {

  A get(UUID uuid, Locale locale) throws IdentifiableServiceException;
}
