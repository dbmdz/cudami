package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.api.identifiable.entity.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Article handling.
 */
@Service
//@Transactional(readOnly = true)
public class ArticleServiceImpl extends EntityServiceImpl<Article> implements ArticleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

  @Autowired
  public ArticleServiceImpl(ArticleRepository repository) {
    super(repository);
  }
}
