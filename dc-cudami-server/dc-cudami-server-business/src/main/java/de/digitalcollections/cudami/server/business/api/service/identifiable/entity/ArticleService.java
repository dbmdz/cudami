package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.Locale;
import java.util.UUID;

/** Service for Article. */
public interface ArticleService extends EntityService<Article> {

  Article get(UUID uuid, Locale locale) throws IdentifiableServiceException;
}
