package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for Article handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class ArticleServiceImpl extends EntityServiceImpl<Article> implements ArticleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

  public ArticleServiceImpl(
      ArticleRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public Article getByUuidAndLocale(UUID uuid, Locale locale) throws IdentifiableServiceException {
    Article article = super.getByUuidAndLocale(uuid, locale);
    if (article == null) {
      return null;
    }

    // filter out not requested translations of fields not already filtered
    if (article.getText() != null) {
      article.getText().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(locale));
    }
    return article;
  }

  @Override
  public List<Agent> getCreators(UUID articleUuid) {
    return ((ArticleRepository) repository).getCreators(articleUuid);
  }
}
