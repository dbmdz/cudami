package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Repository for Article persistence handling. */
public interface ArticleRepository extends EntityRepository<Article> {

  default boolean addCreators(Article article, List<Agent> agents) throws RepositoryException {
    if (article == null || agents == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addCreators(article.getUuid(), agents);
  }

  boolean addCreators(UUID articleUuid, List<Agent> agents) throws RepositoryException;

  default List<Agent> getCreators(Article article) throws RepositoryException {
    if (article == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getCreators(article.getUuid());
  }

  List<Agent> getCreators(UUID articleUuid) throws RepositoryException;

  default boolean removeCreator(Article article, Agent agent) throws RepositoryException {
    if (article == null || agent == null) {
      throw new IllegalArgumentException("remove failed: given objects must not be null");
    }
    return removeCreator(article.getUuid(), agent.getUuid());
  }

  boolean removeCreator(UUID articleUuid, UUID agentUuid) throws RepositoryException;
}
