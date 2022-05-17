package de.digitalcollections.cudami.admin.business.api.service.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.Errors;

/**
 * Service for User.
 *
 * @param <U> domain object
 */
public interface UserService<U extends User> extends UserDetailsService {

  long count() throws ServiceException;

  U create();

  U create(U user, String password1, String password2, Errors results) throws ServiceException;

  U createAdminUser();

  boolean doesActiveAdminUserExist() throws ServiceException;

  PageResponse<U> find(PageRequest pageRequest) throws ServiceException;

  //  List<U> findActiveAdminUsers();
  List<U> findAll() throws ServiceException;

  U getByEmail(String email) throws ServiceException;

  U getByUuid(UUID uuid) throws ServiceException;

  boolean setStatus(UUID uuid, boolean enabled);

  //  U save(U user);
  //  U update(U user);
  U update(U user, String password1, String password2, Errors results) throws ServiceException;
}
