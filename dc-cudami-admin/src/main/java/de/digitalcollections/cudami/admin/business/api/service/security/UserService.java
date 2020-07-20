package de.digitalcollections.cudami.admin.business.api.service.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
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

  long count();

  U activate(UUID uuid) throws ServiceException;

  U create();

  U create(U user, String password1, String password2, Errors results) throws ServiceException;

  U createAdminUser();

  U deactivate(UUID uuid) throws ServiceException;

  boolean doesActiveAdminUserExist() throws ServiceException;

  PageResponse<U> find(PageRequest pageRequest) throws ServiceException;

  //  List<U> findActiveAdminUsers();
  List<U> findAll() throws ServiceException;

  U findByEmail(String email) throws ServiceException;

  U findOne(UUID uuid) throws ServiceException;

  //  U save(U user);
  //  U update(U user);
  U update(U user, String password1, String password2, Errors results) throws ServiceException;
}
