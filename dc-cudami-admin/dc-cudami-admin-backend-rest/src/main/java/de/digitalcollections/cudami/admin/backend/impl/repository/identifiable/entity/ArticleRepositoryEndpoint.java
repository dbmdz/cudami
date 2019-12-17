package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/articles/count")
  long count();

  @RequestLine(
      "GET /latest/articles?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Article> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/articles/{uuid}")
  Article findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/articles/{uuid}?locale={locale}")
  Article findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/articles/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Article findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/articles/{uuid}/children")
  List<Article> getChildren(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/articles")
  @Headers("Content-Type: application/json")
  Article save(Article article);

  @RequestLine("PUT /latest/articles/{uuid}")
  @Headers("Content-Type: application/json")
  Article update(@Param("uuid") UUID uuid, Article article);
}
