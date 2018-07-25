package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.UUID;

public interface WebsiteRepositoryEndpoint {

  @RequestLine("GET /v1/websites?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<WebsiteImpl> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/websites/{uuid}")
  WebsiteImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/websites")
  @Headers("Content-Type: application/json")
  WebsiteImpl save(WebsiteImpl website);

  @RequestLine("PUT /v1/websites/{uuid}")
  @Headers("Content-Type: application/json")
  WebsiteImpl update(@Param("uuid") UUID uuid, WebsiteImpl website);
}
