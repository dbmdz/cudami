package de.digitalcollections.cms.client.backend.impl.repository;

import de.digitalcollections.cms.model.api.security.User;
import feign.RequestLine;
import java.util.List;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /user/v1/findActiveAdminUsers")
  List<User> findActiveAdminUsers();
}
