package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface IdentifiableRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine(
      "GET /latest/identifiables?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Identifiable> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/identifiables?searchTerm={searchTerm}&maxResults={maxResults}")
  List<Identifiable> find(
      @Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine("GET /latest/identifiables/{uuid}")
  Identifiable findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/identifiables/{uuid}?locale={locale}")
  Identifiable findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/identifiables/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Identifiable findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("POST /latest/identifiables")
  @Headers("Content-Type: application/json")
  Identifiable save(Identifiable identifiable);

  @RequestLine("PUT /latest/identifiables/{uuid}")
  @Headers("Content-Type: application/json")
  Identifiable update(@Param("uuid") UUID uuid, Identifiable identifiable);

  @RequestLine("GET /latest/identifiables/count")
  long count();
}
