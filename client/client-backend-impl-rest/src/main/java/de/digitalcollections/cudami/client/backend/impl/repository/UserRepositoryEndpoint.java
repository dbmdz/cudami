package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.security.UserImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /v1/users?role=ADMIN&enabled=true")
  List<UserImpl> findActiveAdminUsers();

  @RequestLine("GET /v1/users?sortOrder={sortOrder}&sortField={sortField}&sortType={sortType}")
  List<UserImpl> findAll(@Param("sortOrder") String sortOrder, @Param("sortField") String sortField, @Param("sortType") String sortType);

  @RequestLine("GET /v1/users?email={email}")
  UserImpl findByEmail(@Param("email") String email);

  @RequestLine("GET /v1/users/{id}")
  UserImpl findOne(@Param("id") Long id);

  @RequestLine("POST /v1/users")
  @Headers("Content-Type: application/json")
  UserImpl save(UserImpl user);

  @RequestLine("PUT /v1/users/{id}")
  @Headers("Content-Type: application/json")
  UserImpl update(@Param("id") Long id, UserImpl user);
}
