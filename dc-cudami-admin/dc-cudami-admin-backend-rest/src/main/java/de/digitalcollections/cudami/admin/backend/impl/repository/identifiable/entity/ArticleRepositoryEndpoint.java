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

  @RequestLine("GET /v1/articles?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Article> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/articles/{uuid}")
  Article findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/articles")
  @Headers("Content-Type: application/json")
  Article save(Article article);

  @RequestLine("PUT /v1/articles/{uuid}")
  @Headers("Content-Type: application/json")
  Article update(@Param("uuid") UUID uuid, Article article);

  @RequestLine("GET /v1/articles/count")
  long count();

  @RequestLine("GET /v1/articles/{uuid}/children")
  List<Article> getChildren(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/articles/{parentArticleUuid}/article")
  @Headers("Content-Type: application/json")
  Article saveWithParent(Article article, @Param("parentArticleUuid") UUID parentArticleUuid);
}
