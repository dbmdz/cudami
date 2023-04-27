package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Repository for Article persistence handling. */
public interface ArticleRepository extends EntityRepository<Article> {

  default List<Agent> getCreators(Article article) throws RepositoryException {
    if (article == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getCreators(article.getUuid());
  }

  List<Agent> getCreators(UUID articleUuid) throws RepositoryException;
}
