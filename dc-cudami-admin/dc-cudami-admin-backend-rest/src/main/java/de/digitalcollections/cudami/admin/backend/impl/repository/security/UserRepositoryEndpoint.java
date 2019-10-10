package de.digitalcollections.cudami.admin.backend.impl.repository.security;

import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.security.UserImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.UUID;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /latest/users?role=ADMIN&enabled=true")
  List<UserImpl> findActiveAdminUsers();

  @RequestLine(
      "GET /latest/users?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<UserImpl> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/users?email={email}")
  UserImpl findByEmail(@Param("email") String email);

  @RequestLine("GET /latest/users/{uuid}")
  UserImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/users")
  @Headers("Content-Type: application/json")
  UserImpl save(UserImpl user);

  @RequestLine("PUT /latest/users/{uuid}")
  @Headers("Content-Type: application/json")
  UserImpl update(@Param("uuid") UUID uuid, UserImpl user);
}
