package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface SubtopicRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/subtopics/count")
  long count();

  @RequestLine(
      "GET /latest/subtopics?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Subtopic> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/subtopics/{uuid}")
  Subtopic findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/subtopics/{uuid}?locale={locale}")
  Subtopic findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/subtopics/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Subtopic findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/subtopics/{uuid}/children")
  List<Subtopic> getChildren(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/subtopics/{uuid}/entities")
  List<Entity> getEntities(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/subtopics/{uuid}/fileresources")
  List<FileResource> getFileResources(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/subtopics/{uuid}/parent")
  Subtopic getParent(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/subtopics")
  @Headers("Content-Type: application/json")
  Subtopic save(Subtopic subtopic);

  @RequestLine("POST /latest/subtopics/{uuid}/entities")
  @Headers("Content-Type: application/json")
  List<Entity> saveEntities(@Param("uuid") UUID uuid, List<Entity> entities);

  @RequestLine("POST /latest/subtopics/{uuid}/fileresources")
  @Headers("Content-Type: application/json")
  List<FileResource> saveFileResources(@Param("uuid") UUID uuid, List<FileResource> fileResources);

  @RequestLine("POST /latest/topics/{parentTopicUuid}/subtopic")
  @Headers("Content-Type: application/json")
  Subtopic saveWithParentTopic(Subtopic subtopic, @Param("parentTopicUuid") UUID parentTopicUuid);

  @RequestLine("POST /latest/subtopics/{parentSubtopicUuid}/subtopic")
  @Headers("Content-Type: application/json")
  Subtopic saveWithParentSubtopic(
      Subtopic subtopic, @Param("parentSubtopicUuid") UUID parentSubtopicUuid);

  @RequestLine("PUT /latest/subtopics/{uuid}")
  @Headers("Content-Type: application/json")
  Subtopic update(@Param("uuid") UUID uuid, Subtopic subtopic);

  @RequestLine("GET /latest/subtopics/entity/{uuid}")
  List<Subtopic> getSubtopicsOfEntity(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/subtopics/fileresource/{uuid}")
  List<Subtopic> getSubtopicsOfFileResource(@Param("uuid") UUID uuid);
}
