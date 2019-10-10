package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface WebsiteRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /latest/websites/count")
  long count();

  @RequestLine(
      "GET /latest/websites?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Website> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/websites/{uuid}")
  Website findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/websites/{uuid}?locale={locale}")
  Website findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/websites/{uuid}/rootPages")
  List<Webpage> getRootPages(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/websites")
  @Headers("Content-Type: application/json")
  Website save(Website website);

  @RequestLine("PUT /latest/websites/{uuid}")
  @Headers("Content-Type: application/json")
  Website update(@Param("uuid") UUID uuid, Website website);
}
