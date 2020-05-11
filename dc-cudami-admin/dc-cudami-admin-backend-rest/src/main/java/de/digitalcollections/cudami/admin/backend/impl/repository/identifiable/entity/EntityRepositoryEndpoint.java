package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface EntityRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("POST /latest/entities/{uuid}/related/fileresources/{fileResourceUuid}")
  void addRelatedFileresource(
      @Param("uuid") UUID uuid, @Param("fileResourceUuid") UUID fileResourceUuid);

  @RequestLine("POST /latest/entities/relations/{subjectEntityUuid}/{predicate}/{objectEntityUuid}")
  public void addRelation(
      @Param("subjectEntityUuid") UUID subjectEntityUuid,
      @Param("predicate") String predicate,
      @Param("objectEntityUuid") UUID objectEntityUuid);

  @RequestLine(
      "GET /latest/entities?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Entity> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/entities?searchTerm={searchTerm}&maxResults={maxResults}")
  List<Entity> find(@Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine(
      "GET /latest/entities?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<Entity> find(
      @Param("searchTerm") String searchTerm,
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/entities/{uuid}")
  Entity findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entities/{uuid}?locale={locale}")
  Entity findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/entities/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Entity findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/entities/{refId}")
  @Headers("Accept: application/json")
  Entity findOneByRefId(@Param("refId") long refId);

  @RequestLine("GET /latest/entities/{uuid}/related/fileresources")
  List<FileResource> getRelatedFileResources(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entities/relations/{subjectEntityUuid}")
  List<EntityRelation> getRelations(@Param("subjectEntityUuid") UUID subjectEntityUuid);

  @RequestLine("POST /latest/entities")
  @Headers("Content-Type: application/json")
  Entity save(Entity entity);

  @RequestLine("POST /latest/entities/{uuid}/related/fileresources")
  List<FileResource> saveRelatedFileResources(
      @Param("uuid") UUID uuid, List<FileResource> fileResources);

  @RequestLine("POST /latest/entities/relations")
  List<EntityRelation> saveRelations(List<EntityRelation> relations);

  @RequestLine("PUT /latest/entities/{uuid}")
  @Headers("Content-Type: application/json")
  Entity update(@Param("uuid") UUID uuid, Entity entity);

  @RequestLine("GET /latest/entities/count")
  long count();
}
