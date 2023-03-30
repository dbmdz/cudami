package de.digitalcollections.cudami.server.business.api.service.security;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.security.User;
import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Service for User. */
public interface UserService extends UniqueObjectService<User> {

  User activate(User user) throws ServiceException;

  User createAdminUser() throws ServiceException;

  User deactivate(User user) throws ServiceException;

  boolean doesActiveAdminUserExist() throws ServiceException;

  List<User> getActiveAdminUsers() throws ServiceException;

  User getByUsername(String username) throws ServiceException, UsernameNotFoundException;
}
