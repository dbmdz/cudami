package de.digitalcollections.cudami.server.business.impl.service.security;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.cudami.server.business.impl.validator.UniqueUsernameValidator;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/** Service for User handling. */
@Service
// @Transactional(readOnly = true)
public class UserServiceImpl extends UniqueObjectServiceImpl<User, UserRepository>
    implements UserService, InitializingBean {

  private UniqueUsernameValidator uniqueUsernameValidator;

  public UserServiceImpl(UserRepository userRepository) {
    super(userRepository);
  }

  @Override
  // @Transactional(readOnly = false)
  public User activate(User user) throws ServiceException, ValidationException {
    User userFromRepo;
    try {
      userFromRepo = repository.getByExample(user);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    userFromRepo.setEnabled(true);
    try {
      repository.save(userFromRepo);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return userFromRepo;
  }

  @Override
  public void afterPropertiesSet() {
    this.uniqueUsernameValidator = new UniqueUsernameValidator(this);
  }

  @Override
  public User createAdminUser() throws ServiceException {
    User user;
    try {
      user = repository.create();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    user.getRoles().add(Role.ADMIN);
    return user;
  }

  @Override
  // @Transactional(readOnly = false)
  public User deactivate(User user) throws ServiceException, ValidationException {
    User userFromRepo;
    try {
      userFromRepo = repository.getByExample(user);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    userFromRepo.setEnabled(false);
    try {
      repository.save(userFromRepo);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return userFromRepo;
  }

  @Override
  public boolean doesActiveAdminUserExist() throws ServiceException {
    List findActiveAdminUsers;
    try {
      findActiveAdminUsers = repository.getActiveAdminUsers();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    if (findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  // @Transactional(readOnly = true)
  public List<User> getActiveAdminUsers() throws ServiceException {
    try {
      return repository.getActiveAdminUsers();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  /*
   * see:
   * http://stackoverflow.com/questions/19302196/transaction-marked-as-rollback-
   * only-how-do-i-find-the-cause When you mark your method as @Transactional,
   * occurrence of any exception inside your method will mark the surrounding TX
   * as roll-back only (even if you catch them). You can use other attributes of
   *
   * @Transactional annotation to prevent it of rolling back like:
   *
   * @Transactional(rollbackFor=MyException.class,
   * noRollbackFor=MyException2.class)
   */
  @Override
  // @Transactional(readOnly = true, noRollbackFor =
  // UsernameNotFoundException.class)
  public User getByUsername(String username) throws UsernameNotFoundException, ServiceException {
    User user;
    try {
      user = repository.getByEmail(username);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    return user;
  }

  @Override
  // @Transactional(readOnly = false)
  public User save(User user, Errors results) throws ServiceException, ValidationException {
    uniqueUsernameValidator.validate(user, results);
    if (!results.hasErrors()) {
      try {
        repository.save(user);
      } catch (RepositoryException e) {
        throw new ServiceException("Backend failure", e);
      }
      return user;
    }
    return null;
  }

  @Override
  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "email");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  // @Transactional(readOnly = false)
  public User update(User user, Errors results) throws ServiceException, ValidationException {
    try {
      repository.update(user);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return user;
  }
}
