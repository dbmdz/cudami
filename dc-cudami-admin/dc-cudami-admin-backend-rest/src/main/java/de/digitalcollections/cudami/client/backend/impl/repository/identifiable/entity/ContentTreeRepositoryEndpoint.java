package de.digitalcollections.cudami.client.backend.impl.repository.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.impl.identifiable.entity.ContentTreeImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.UUID;

public interface ContentTreeRepositoryEndpoint {

  @RequestLine("GET /v1/contenttrees?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<ContentTreeImpl> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/contenttrees/{uuid}")
  ContentTreeImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/contenttrees")
  @Headers("Content-Type: application/json")
  ContentTreeImpl save(ContentTreeImpl contentTree);

  @RequestLine("PUT /v1/contenttrees/{uuid}")
  @Headers("Content-Type: application/json")
  ContentTreeImpl update(@Param("uuid") UUID uuid, ContentTreeImpl contentTree);
}
