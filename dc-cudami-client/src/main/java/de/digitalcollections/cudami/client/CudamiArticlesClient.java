package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiArticlesClient extends CudamiBaseClient<Article> {

  public CudamiArticlesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Article.class, mapper);
  }

  public Article create() {
    return new Article();
  }

  long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/articles/count"));
  }

  public PageResponse<Article> find(PageRequest pageRequest) throws HttpException {
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
    return doGetRequestForObjectList(String.format("/latest/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  public Article save(Article article) throws HttpException {
    return doPostRequestForObject("/latest/articles", (Article) article);
  }

  public Article update(UUID uuid, Article article) throws HttpException {
    return doPutRequestForObject(String.format("/latest/articles/%s", uuid), (Article) article);
  }
}
