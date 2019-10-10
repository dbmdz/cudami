package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl extends EntityRepositoryImpl<Article>
    implements ArticleRepository {

  @Autowired private ArticleRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public Article create() {
    return new ArticleImpl();
  }

  @Override
  public PageResponse<Article> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Article> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public Article findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public Article findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public Article save(Article identifiable) {
    return endpoint.save(identifiable);
  }

  @Override
  public Article update(Article identifiable) {
    return endpoint.update(identifiable.getUuid(), identifiable);
  }
}
