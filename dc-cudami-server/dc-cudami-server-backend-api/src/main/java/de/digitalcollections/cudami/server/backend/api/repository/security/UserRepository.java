package de.digitalcollections.cudami.server.backend.api.repository.security;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.util.List;
import java.util.UUID;

/** Repository for User persistence handling. */
public interface UserRepository {

  long count();

  User create();

  PageResponse<User> find(PageRequest pageRequest);

  List<User> getActiveAdminUsers();

  User getByEmail(String email);

  User getByUuid(UUID uuid);

  User save(User user);

  User update(User user);
}
