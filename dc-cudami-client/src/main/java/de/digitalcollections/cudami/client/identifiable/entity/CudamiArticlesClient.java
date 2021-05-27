package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiArticlesClient extends CudamiBaseClient<Article> {

  public CudamiArticlesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Article.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/articles/count"));
  }

  public Article create() {
    return new Article();
  }

  public PageResponse<Article> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/articles", pageRequest);
  }

  public SearchPageResponse<Article> find(SearchPageRequest pageRequest) throws HttpException {
    return this.doGetSearchRequestForPagedObjectList("/v5/articles", pageRequest);
  }

  public Article findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/articles/%s", uuid));
  }

  public Article findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Article findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/articles/%s?locale=%s", uuid, locale));
  }

  public Article findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/articles/identifier/%s:%s.json", namespace, id));
  }

  public List<Locale> getLanguages() throws HttpException {
    return this.doGetRequestForObjectList("/v5/articles/languages", Locale.class);
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  public Article save(Article article) throws HttpException {
    return doPostRequestForObject("/v5/articles", article);
  }

  public Article update(UUID uuid, Article article) throws HttpException {
    return doPutRequestForObject(String.format("/v5/articles/%s", uuid), article);
  }
}
