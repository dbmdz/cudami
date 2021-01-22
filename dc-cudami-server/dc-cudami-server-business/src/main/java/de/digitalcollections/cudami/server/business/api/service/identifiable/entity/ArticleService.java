package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Service for Article. */
public interface ArticleService extends EntityService<Article> {

  List<Agent> getCreators(UUID articleUuid);
}
