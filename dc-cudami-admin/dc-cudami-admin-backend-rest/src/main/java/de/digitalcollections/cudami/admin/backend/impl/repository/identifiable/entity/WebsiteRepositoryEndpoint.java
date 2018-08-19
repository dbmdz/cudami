package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface WebsiteRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /v1/websites?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Website> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/websites/{uuid}")
  Website findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/websites")
  @Headers("Content-Type: application/json")
  Website save(Website website);

  @RequestLine("PUT /v1/websites/{uuid}")
  @Headers("Content-Type: application/json")
  Website update(@Param("uuid") UUID uuid, Website website);

  @RequestLine("GET /v1/websites/count")
  long count();

  @RequestLine("GET /v1/websites/{uuid}/rootPages")
  List<Webpage> getRootPages(@Param("uuid") UUID uuid);
}
