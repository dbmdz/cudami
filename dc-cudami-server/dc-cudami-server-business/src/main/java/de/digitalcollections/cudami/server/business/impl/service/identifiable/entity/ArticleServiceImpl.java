package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Article handling.
 *
 * @param <I> identifiable instance
 */
@Service
//@Transactional(readOnly = true)
public class ArticleServiceImpl<I extends Identifiable> extends EntityServiceImpl<Article> implements ArticleService<Article, I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  @Autowired
  public ArticleServiceImpl(ArticleRepository<Article, I> repository) {
    super(repository);
  }

  @Override
  public void addIdentifiable(UUID identifiablesContainerUuid, UUID identifiableUuid) {
    // FIXME: implement it
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Article get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    Article article = repository.findOne(uuid, locale);

    // artilce does not exist in requested language, so try with default locale
    if (article == null) {
      article = repository.findOne(uuid, localeService.getDefault());
    }

    // article does not exist in default locale, so just return first existing language
    if (article == null) {
      article = repository.findOne(uuid, null);
    }

    return article;
  }

  @Override
  public List<Identifiable> getIdentifiables(Article article) {
    return getIdentifiables(article.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(UUID identifiableUuid) {
    return ((ArticleRepository) repository).getIdentifiables(identifiableUuid);
  }

  @Override
  public List<Identifiable> saveIdentifiables(Article article, List<Identifiable> identifiables) {
    return saveIdentifiables(article.getUuid(), identifiables);
  }

  @Override
  public List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables) {
    return ((ArticleRepository) repository).saveIdentifiables(identifiablesContainerUuid, identifiables);
  }
}
