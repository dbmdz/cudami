package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.cudami.model.impl.security.UserImpl;
import feign.RequestLine;
import java.util.List;

public interface UserRepositoryEndpoint {

  @RequestLine("GET /user/v1/findActiveAdminUsers")
  List<UserImpl> findActiveAdminUsers();
}
