package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.security.UserImpl;
import feign.Param;
import feign.RequestLine;
import java.util.List;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /user/v1/findActiveAdminUsers")
  List<UserImpl> findActiveAdminUsers();

  @RequestLine("GET /user/v1/findByEmail/{email}")
  UserImpl findByEmail(@Param("email") String email);
}
