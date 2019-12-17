package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.form.FormData;
import java.util.UUID;

public interface CudamiFileResourceRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/fileresources/count")
  long count();

  @RequestLine(
      "GET /latest/fileresources?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<FileResource> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/fileresources/{uuid}")
  FileResource findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/fileresources/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  FileResource findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  /**
   * only saving non binary parts of resource
   *
   * @param fileresource metadata object
   * @return saved object
   */
  @RequestLine("POST /latest/fileresources")
  @Headers("Content-Type: application/json")
  FileResource save(FileResource fileresource);

  @RequestLine("POST /latest/fileresources")
  @Headers("Content-Type: multipart/form-data")
  FileResource save(
      @Param("fileresource") FileResource fileresource, @Param("binaryData") FormData binaryData);

  @RequestLine("PUT /latest/fileresources/{uuid}")
  @Headers("Content-Type: application/json")
  FileResource update(@Param("uuid") UUID uuid, FileResource fileresource);
}
