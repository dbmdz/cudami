package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface ContentNodeRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /v1/contentnodes?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<ContentNode> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/contentnodes/{uuid}")
  ContentNode findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/contentnodes")
  @Headers("Content-Type: application/json")
  ContentNode save(ContentNode contentNode);

  @RequestLine("PUT /v1/contentnodes/{uuid}")
  @Headers("Content-Type: application/json")
  ContentNode update(@Param("uuid") UUID uuid, ContentNode contentNode);

  @RequestLine("GET /v1/contentnodes/count")
  long count();

  @RequestLine("GET /v1/contentnodes/{uuid}/children")
  List<ContentNode> getChildren(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/contenttrees/{parentContentTreeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNode saveWithParentContentTree(ContentNode contentNode, @Param("parentContentTreeUuid") UUID parentContentTreeUuid);

  @RequestLine("POST /v1/contentnodes/{parentContentNodeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNode saveWithParentContentNode(ContentNode contentNode, @Param("parentContentNodeUuid") UUID parentContentNodeUuid);
  
  @RequestLine("GET /v1/contentnodes/{uuid}/identifiables")
  public List<Identifiable> getIdentifiables(UUID uuid);
}
