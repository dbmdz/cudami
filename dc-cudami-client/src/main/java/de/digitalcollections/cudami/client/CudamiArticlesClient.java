package de.digitalcollections.cudami.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiArticlesClient extends CudamiBaseClient<ArticleImpl> {

  public CudamiArticlesClient(String serverUrl) {
    super(serverUrl, ArticleImpl.class);
  }

  public Article create() {
    return new ArticleImpl();
  }

  long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/articles/count"));
  }

  public PageResponse<ArticleImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/articles", pageRequest);
  }

  public Article findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/articles/%s", uuid));
  }

  public Article findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Article findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/articles/%s?locale=%s", uuid, locale));
  }

  public Article findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/articles/identifier/%s:%s.json", namespace, id));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public Article save(Article article) throws HttpException {
    return doPostRequestForObject("/latest/articles", (ArticleImpl) article);
  }

  public Article update(UUID uuid, Article article) throws HttpException {
    return doPutRequestForObject(String.format("/latest/articles/%s", uuid), (ArticleImpl) article);
  }
}
