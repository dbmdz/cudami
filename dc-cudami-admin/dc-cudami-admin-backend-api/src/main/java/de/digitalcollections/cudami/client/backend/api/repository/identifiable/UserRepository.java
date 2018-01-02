package de.digitalcollections.cudami.client.backend.api.repository.identifiable;

import de.digitalcollections.cudami.model.api.security.User;
import java.util.List;

/**
 * Repository for User persistence handling.
 * @param <U> implementation of User interface
 */
public interface UserRepository<U extends User> extends IdentifiableRepository<U> {

  List<U> findAll();

  List<U> findActiveAdminUsers();

  U findByEmail(String email);
}
