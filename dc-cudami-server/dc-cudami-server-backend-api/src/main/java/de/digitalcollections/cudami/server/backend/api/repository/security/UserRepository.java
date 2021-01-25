package de.digitalcollections.cudami.server.backend.api.repository.security;

import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import java.util.List;
import java.util.UUID;

/** Repository for User persistence handling. */
public interface UserRepository {

  long count();

  User create();

  PageResponse<User> find(PageRequest pageRequest);

  List<User> findActiveAdminUsers();

  User findByEmail(String email);

  User findOne(UUID uuid);

  User save(User user);

  User update(User user);
}
