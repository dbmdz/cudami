package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.security.RoleImpl;
import feign.Param;
import feign.RequestLine;

public interface RoleRepositoryEndpoint {
  @RequestLine("GET /role/v1/findByName/{name}")
  RoleImpl findByName(@Param("name") String name);
}
