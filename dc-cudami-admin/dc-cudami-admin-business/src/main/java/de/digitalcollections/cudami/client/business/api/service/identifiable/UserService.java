package de.digitalcollections.cudami.client.business.api.service.identifiable;

import de.digitalcollections.cudami.model.api.security.User;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.Errors;

/**
 * Service for User.
 *
 * @param <T> domain object
 */
public interface UserService<T extends User> extends UserDetailsService, IdentifiableService<T> {

  T activate(UUID uuid);

  T create(T user, String password1, String password2, Errors results);

  T createAdminUser();

  T deactivate(UUID uuid);

  List<T> getAll();

  T update(T user, String password1, String password2, Errors results);

  boolean doesActiveAdminUserExist();
}
