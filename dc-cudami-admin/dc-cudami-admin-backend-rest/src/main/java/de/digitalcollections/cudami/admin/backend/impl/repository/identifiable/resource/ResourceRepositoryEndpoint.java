package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;
import java.util.UUID;

public interface ResourceRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /v1/resources?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Resource> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/resources/{uuid}")
  Resource findOne(@Param("uuid") UUID uuid);

  /**
   * only saving non binary parts of resource
   *
   * @param resource metadata object
   * @return saved object
   */
  @RequestLine("POST /v1/resources")
  @Headers("Content-Type: application/json")
  Resource save(Resource resource);

  @RequestLine("POST /v1/resources")
  @Headers("Content-Type: multipart/form-data")
  Resource save(@Param("fileresource") FileResource fileresource, @Param("binaryData") FormData binaryData);

  @RequestLine("PUT /v1/resources/{uuid}")
  @Headers("Content-Type: application/json")
  Resource update(@Param("uuid") UUID uuid, Resource resource);

  @RequestLine("GET /v1/resources/count")
  long count();
}
