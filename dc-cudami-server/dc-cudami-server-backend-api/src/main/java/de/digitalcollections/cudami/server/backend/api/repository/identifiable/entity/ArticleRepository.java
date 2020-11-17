package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Repository for Article persistence handling. */
public interface ArticleRepository extends EntityRepository<Article> {

  List<Agent> getCreators(UUID articleUuid);
}
