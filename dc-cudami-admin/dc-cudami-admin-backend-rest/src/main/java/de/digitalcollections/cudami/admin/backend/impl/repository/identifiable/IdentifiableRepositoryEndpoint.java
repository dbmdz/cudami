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

  @RequestLine("GET /v1/identifiables?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Identifiable> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/identifiables?searchTerm={searchTerm}&maxResults={maxResults}")
  List<Identifiable> find(@Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine("GET /v1/identifiables/{uuid}")
  Identifiable findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/identifiables")
  @Headers("Content-Type: application/json")
  Identifiable save(Identifiable identifiable);

  @RequestLine("PUT /v1/identifiables/{uuid}")
  @Headers("Content-Type: application/json")
  Identifiable update(@Param("uuid") UUID uuid, Identifiable identifiable);

  @RequestLine("GET /v1/identifiables/count")
  long count();
}
