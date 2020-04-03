package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface TopicRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/topics/count")
  long count();

  @RequestLine(
      "GET /latest/topics?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Topic> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/topics/{uuid}")
  Topic findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/topics/{uuid}?locale={locale}")
  Topic findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/topics/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Topic findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/topics/{uuid}/subtopics")
  List<Subtopic> getSubtopics(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/topics")
  @Headers("Content-Type: application/json")
  Topic save(Topic topic);

  @RequestLine("PUT /latest/topics/{uuid}")
  @Headers("Content-Type: application/json")
  Topic update(@Param("uuid") UUID uuid, Topic topic);
}
