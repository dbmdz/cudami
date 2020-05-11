package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface FileResourceMetadataRepositoryEndpoint extends RepositoryEndpoint {

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

  @RequestLine("GET /latest/fileresources?searchTerm={searchTerm}&maxResults={maxResults}")
  List<FileResource> find(
      @Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine(
      "GET /latest/fileresources?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<FileResource> find(
      @Param("searchTerm") String searchTerm,
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine(
      "GET /latest/fileresources/images?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<FileResource> findImages(
      @Param("searchTerm") String searchTerm,
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

  @RequestLine("POST /latest/fileresources")
  @Headers("Content-Type: application/json")
  FileResource save(FileResource fileresource);

  @RequestLine("PUT /latest/fileresources/{uuid}")
  @Headers("Content-Type: application/json")
  FileResource update(@Param("uuid") UUID uuid, FileResource fileresource);
}
