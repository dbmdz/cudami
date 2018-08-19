package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.UUID;

/**
 * Repository for Article persistence handling.
 *
 * @param <A> resource instance
 */
public interface ArticleRepository<A extends Article> extends EntityRepository<A>, NodeRepository<A> {

  Article saveWithParent(A article, UUID parentUuid);
}
