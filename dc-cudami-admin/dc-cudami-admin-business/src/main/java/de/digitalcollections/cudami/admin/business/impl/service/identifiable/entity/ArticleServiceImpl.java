package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ArticleService;
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
 */
@Service
//@Transactional(readOnly = true)
public class ArticleServiceImpl extends EntityServiceImpl<Article> implements ArticleService<Article> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  @Autowired
  public ArticleServiceImpl(ArticleRepository<Article> repository) {
    super(repository);
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
  public List<Article> getChildren(Article article) {
    return ((NodeRepository) repository).getChildren(article);
  }

  @Override
  public List<Article> getChildren(UUID uuid) {
    return ((NodeRepository) repository).getChildren(uuid);
  }

  @Override
  //  @Transactional(readOnly = false)
  public Article saveWithParent(Article article, UUID parentUuid) throws IdentifiableServiceException {
    try {
      return ((ArticleRepository) repository).saveWithParent(article, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save article " + article + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public void addContent(Article article, Identifiable identifiable) {
    ((NodeRepository) repository).addContent(article, identifiable);
  }

  public List<Identifiable> getIdentifiables(Article article) {
    return ((ArticleRepository) repository).getIdentifiables(article);
  }
}
