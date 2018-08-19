package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.paging.PageResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.UUID;

public interface EntityRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("GET /v1/entities?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Entity> find(
          @Param("pageNumber") int pageNumber, @Param("pageSize") int pageSize,
          @Param("sortField") String sortField, @Param("sortDirection") String sortDirection, @Param("nullHandling") String nullHandling
  );

  @RequestLine("GET /v1/entities/{uuid}")
  Entity findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /v1/entities")
  @Headers("Content-Type: application/json")
  Entity save(Entity entity);

  @RequestLine("PUT /v1/entities/{uuid}")
  @Headers("Content-Type: application/json")
  Entity update(@Param("uuid") UUID uuid, Entity entity);

  @RequestLine("GET /v1/entities/count")
  long count();
}
