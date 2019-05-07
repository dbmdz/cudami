package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.LinkedHashSet;
import java.util.UUID;

public interface DigitalObjectRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/digitalobjects/count")
  long count();

  @RequestLine("GET /latest/digitalobjects?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<DigitalObject> find(
      @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
      @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /latest/digitalobjects/{uuid}")
  DigitalObject findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/digitalobjects/{uuid}?locale={locale}")
  DigitalObject findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/digitalobjects/{uuid}/fileresources")
  LinkedHashSet<FileResource> getFileResources(UUID uuid);

  @RequestLine("POST /latest/digitalobjects")
  @Headers("Content-Type: application/json")
  DigitalObject save(DigitalObject digitalObject);

  @RequestLine("POST /latest/digitalobjects/{uuid}/fileresources")
  LinkedHashSet<FileResource> saveFileResources(UUID uuid, LinkedHashSet<FileResource> fileResources);

  @RequestLine("PUT /latest/digitalobjects/{uuid}")
  @Headers("Content-Type: application/json")
  DigitalObject update(@Param("uuid") UUID uuid, DigitalObject digitalObject);
}
