package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface ContentTreeRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/contenttrees?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<ContentTree> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /latest/contenttrees/{uuid}")
  ContentTree findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/contenttrees")
  @Headers("Content-Type: application/json")
  ContentTree save(ContentTree contentTree);

  @RequestLine("PUT /latest/contenttrees/{uuid}")
  @Headers("Content-Type: application/json")
  ContentTree update(@Param("uuid") UUID uuid, ContentTree contentTree);

  @RequestLine("GET /latest/contenttrees/count")
  long count();

  @RequestLine("GET /latest/contenttrees/{uuid}/rootNodes")
  List<ContentNode> getRootNodes(@Param("uuid") UUID uuid);
}
