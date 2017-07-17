package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.security.UserImpl;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /user/v1/create")
  UserImpl create();

  @RequestLine("GET /user/v1/findActiveAdminUsers")
  List<UserImpl> findActiveAdminUsers();

  @RequestLine("GET /user/v1/findAll")
  List<UserImpl> findAll(@Param("sortOrder") String sortOrder, @Param("sortField") String sortField, @Param("sortType") String sortType);

  @RequestLine("GET /user/v1/findByEmail/{email}")
  UserImpl findByEmail(@Param("email") String email);

  @RequestLine("GET /user/v1/{id}")
  UserImpl findOne(@Param("id") Long id);

  @RequestLine("POST /user/v1/save")
  @Headers("Content-Type: application/json")
  UserImpl save(UserImpl user);
}
