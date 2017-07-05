package de.digitalcollections.cms.client.backend.impl.repository;

import de.digitalcollections.cms.model.impl.security.RoleImpl;
import feign.Param;
import feign.RequestLine;

public interface RoleRepositoryEndpoint {
  @RequestLine("GET /role/v1/findByName/{name}")
  RoleImpl findByName(@Param("name") String name);
}
