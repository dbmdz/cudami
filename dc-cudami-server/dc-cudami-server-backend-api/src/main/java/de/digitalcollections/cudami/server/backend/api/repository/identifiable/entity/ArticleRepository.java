package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiablesContainerRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;

/**
 * Repository for Article persistence handling.
 *
 * @param <A> article instance
 * @param <I> identifiable isntance
 */
public interface ArticleRepository<A extends Article, I extends Identifiable> extends EntityRepository<A>, IdentifiablesContainerRepository<A, I> {

}
