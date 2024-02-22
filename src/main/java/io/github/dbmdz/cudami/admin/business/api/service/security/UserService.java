package io.github.dbmdz.cudami.admin.business.api.service.security;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for User.
 *
 * @param <U> domain object
 */
public interface UserService<U extends User> {

  long count() throws ServiceException;

  U create() throws ServiceException;

  U create(U user, String password1, String password2, Errors results) throws ServiceException;

  U createAdminUser() throws ServiceException;

  boolean doesActiveAdminUserExist() throws ServiceException;

  PageResponse<U> find(PageRequest pageRequest) throws ServiceException;

  //  List<U> findActiveAdminUsers();

  U getByEmail(String email) throws ServiceException;

  U getByUuid(UUID uuid) throws ServiceException;

  boolean setStatus(UUID uuid, boolean enabled);

  //  U save(U user);
  //  U update(U user);
  U update(U user, String password1, String password2, Errors results) throws ServiceException;
}
