package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface IdentifierTypeRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine(
      "GET /latest/identifiertypes?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<IdentifierType> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/identifiertypes?searchTerm={searchTerm}&maxResults={maxResults}")
  List<IdentifierType> find(
      @Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine("GET /latest/identifiertypes/{uuid}")
  IdentifierType findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/identifiertypes/{uuid}?locale={locale}")
  IdentifierType findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("POST /latest/identifiertypes")
  @Headers("Content-Type: application/json")
  IdentifierType save(IdentifierType identifierType);

  @RequestLine("PUT /latest/identifiertypes/{uuid}")
  @Headers("Content-Type: application/json")
  IdentifierType update(@Param("uuid") UUID uuid, IdentifierType identifierType);
}
