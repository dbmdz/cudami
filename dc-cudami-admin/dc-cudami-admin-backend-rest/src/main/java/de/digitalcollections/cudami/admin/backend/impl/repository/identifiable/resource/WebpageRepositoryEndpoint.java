package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.WebpageImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.UUID;

public interface WebpageRepositoryEndpoint {

  @RequestLine("GET /v1/webpages?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<WebpageImpl> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/webpages/{uuid}")
  WebpageImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/webpages")
  @Headers("Content-Type: application/json")
  WebpageImpl save(WebpageImpl webpage);

  @RequestLine("POST /v1/websites/{parentWebsiteUuid}/webpage")
  @Headers("Content-Type: application/json")
  WebpageImpl saveWithParentWebsite(WebpageImpl webpage, @Param("parentWebsiteUuid") UUID parentWebsiteUuid);

  @RequestLine("POST /v1/webpages/{parentWebpageUuid}/webpage")
  @Headers("Content-Type: application/json")
  WebpageImpl saveWithParentWebpage(WebpageImpl webpage, @Param("parentWebpageUuid") UUID parentWebpageUuid);

  @RequestLine("PUT /v1/webpages/{uuid}")
  @Headers("Content-Type: application/json")
  WebpageImpl update(@Param("uuid") UUID uuid, WebpageImpl webpage);
}
