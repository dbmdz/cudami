package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl<A extends Article> extends EntityRepositoryImpl<A> implements ArticleRepository<A> {

  @Autowired
  private LocaleRepository localeRepository;

  @Autowired
  private ArticleRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public A create() {
    Locale defaultLocale = localeRepository.getDefault();
    A article = (A) new ArticleImpl();
    article.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    article.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    article.setText(new LocalizedStructuredContentImpl(defaultLocale));
    return article;
  }

  @Override
  public PageResponse<A> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Article> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public A findOne(UUID uuid) {
    return (A) endpoint.findOne(uuid);
  }

  @Override
  public A save(A identifiable) {
    return (A) endpoint.save(identifiable);
  }

  @Override
  public A update(A identifiable) {
    return (A) endpoint.update(identifiable.getUuid(), identifiable);
  }

  @Override
  public Article saveWithParent(A article, UUID parentUuid) {
    return (A) endpoint.saveWithParent(article, parentUuid);
  }

  @Override
  public List<A> getChildren(A article) {
    return getChildren(article.getUuid());
  }

  @Override
  public List<A> getChildren(UUID uuid) {
    return (List<A>) endpoint.getChildren(uuid);
  }

  @Override
  public void addContent(A article, Identifiable identifiable) {
    addContent(article, identifiable.getUuid());
  }
  
  private void addContent(A article, UUID uuid) {
    endpoint.addContent(article, uuid);
  }
}
