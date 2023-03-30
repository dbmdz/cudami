package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;

/** Service for Article. */
public interface ArticleService extends EntityService<Article> {

  List<Agent> getCreators(Article article) throws ServiceException;
}
