package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiArticlesClient extends CudamiBaseClient<ArticleImpl> {

  public CudamiArticlesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, ArticleImpl.class, mapper);
  }

  public Article create() {
    return new ArticleImpl();
  }

  long count() throws HttpException {
    // No GET endpoint for /latest/articles/count available!
    throw new HttpException("/latest/articles/count", 404);
  }

  public PageResponse<ArticleImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/articles", pageRequest);
  }

  public Article findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/articles/%s", uuid));
  }

  public Article findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Article findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/articles/%s?locale=%s", uuid, locale));
  }

  public Article findOneByIdentifier(String namespace, String id) throws HttpException {
    // URL /latest/articles/identifier/%s:%s.json does not exist yet
    throw new HttpException(
        String.format("/latest/articles/identifier/%s:%s.json", namespace, id), 404);
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public Article save(Article article) throws HttpException {
    return doPostRequestForObject("/v2/articles", (ArticleImpl) article);
  }

  public Article update(UUID uuid, Article article) throws HttpException {
    return doPutRequestForObject(String.format("/v2/articles/%s", uuid), (ArticleImpl) article);
  }
}
