package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public interface ContentNodeRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/contentnodes/count")
  long count();

  @RequestLine("GET /latest/contentnodes?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<ContentNode> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /latest/contentnodes/{uuid}")
  ContentNode findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/contentnodes/{uuid}?locale={locale}")
  ContentNode findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/contentnodes/{uuid}/children")
  List<ContentNode> getChildren(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/contentnodes/{uuid}/entities")
  LinkedHashSet<Entity> getEntities(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/contentnodes/{uuid}/fileresources")
  LinkedHashSet<FileResource> getFileResources(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/contentnodes")
  @Headers("Content-Type: application/json")
  ContentNode save(ContentNode contentNode);

  @RequestLine("POST /latest/contentnodes/{uuid}/entities")
  @Headers("Content-Type: application/json")
  LinkedHashSet<Entity> saveEntities(@Param("uuid") UUID uuid, LinkedHashSet<Entity> entities);

  @RequestLine("POST /latest/contentnodes/{uuid}/fileresources")
  @Headers("Content-Type: application/json")
  LinkedHashSet<FileResource> saveFileResources(@Param("uuid") UUID uuid, LinkedHashSet<FileResource> fileResources);

  @RequestLine("POST /latest/contenttrees/{parentContentTreeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNode saveWithParentContentTree(ContentNode contentNode, @Param("parentContentTreeUuid") UUID parentContentTreeUuid);

  @RequestLine("POST /latest/contentnodes/{parentContentNodeUuid}/contentnode")
  @Headers("Content-Type: application/json")
  ContentNode saveWithParentContentNode(ContentNode contentNode, @Param("parentContentNodeUuid") UUID parentContentNodeUuid);

  @RequestLine("PUT /latest/contentnodes/{uuid}")
  @Headers("Content-Type: application/json")
  ContentNode update(@Param("uuid") UUID uuid, ContentNode contentNode);
}
