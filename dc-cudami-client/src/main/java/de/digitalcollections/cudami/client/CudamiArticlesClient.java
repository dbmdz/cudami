package de.digitalcollections.cudami.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;

public class CudamiArticlesClient extends CudamiBaseClient {

  public CudamiArticlesClient(String serverUrl) {
    super(serverUrl, ArticleImpl.class);
  }

  public Article create() {
    return new ArticleImpl();
  }

  long count() throws HttpException, Exception {
    return Long.parseLong(doGetRequestForString("/latest/articles/count"));
  }

  //  @RequestLine(
  //          "GET
  // /latest/articles?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  //  PageResponse<Article> find(
  //          @Param("pageNumber") int pageNumber,
  //          @Param("pageSize") int pageSize,
  //          @Param("sortField") String sortField,
  //          @Param("sortDirection") String sortDirection,
  //          @Param("nullHandling") String nullHandling);
  //
  //  @RequestLine("GET /latest/articles/{uuid}")
  //  Article findOne(@Param("uuid") UUID uuid);
  //
  //  @RequestLine("GET /latest/articles/{uuid}?locale={locale}")
  //  Article findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);
  //
  //  @RequestLine("GET /latest/articles/identifier/{namespace}:{id}.json")
  //  @Headers("Accept: application/json")
  //  Article findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);
  //
  //  @RequestLine("GET /latest/articles/{uuid}/children")
  //  List<Article> getChildren(@Param("uuid") UUID uuid);
  //
  //  // TODO: move to EntityClient and extends ...
  //  @RequestLine("GET /latest/entities/{uuid}/related/fileresources")
  //  List<FileResource> getRelatedFileResources(@Param("uuid") UUID uuid);
  //
  //  @RequestLine("POST /latest/articles")
  //  @Headers("Content-Type: application/json")
  //  Article save(Article article);
  //
  //  @RequestLine("PUT /latest/articles/{uuid}")
  //  @Headers("Content-Type: application/json")
  //  Article update(@Param("uuid") UUID uuid, Article article);
}
