package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.ContentNodeImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.UUID;

public interface ContentNodeRepositoryEndpoint {

  @RequestLine("GET /v1/contentnodes?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<ContentNodeImpl> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/contentnodes/{uuid}")
  ContentNodeImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/contentnodes")
  @Headers("Content-Type: application/json")
  ContentNodeImpl save(ContentNodeImpl contentNode);

  @RequestLine("POST /v1/contenttrees/{parentContentTreeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNodeImpl saveWithParentContentTree(ContentNodeImpl contentNode, @Param("parentContentTreeUuid") UUID parentContentTreeUuid);

  @RequestLine("POST /v1/contentnodes/{parentContentNodeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNodeImpl saveWithParentContentNode(ContentNodeImpl contentNode, @Param("parentContentNodeUuid") UUID parentContentNodeUuid);

  @RequestLine("PUT /v1/contentnodes/{uuid}")
  @Headers("Content-Type: application/json")
  ContentNodeImpl update(@Param("uuid") UUID uuid, ContentNodeImpl contentNode);
}
