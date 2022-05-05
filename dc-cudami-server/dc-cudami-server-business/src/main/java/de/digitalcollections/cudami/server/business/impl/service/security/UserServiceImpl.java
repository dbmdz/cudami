package de.digitalcollections.cudami.server.business.impl.service.security;

import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import de.digitalcollections.cudami.server.business.impl.validator.UniqueUsernameValidator;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/** Service for User handling. */
@Service
// @Transactional(readOnly = true)
public class UserServiceImpl implements UserService<User>, InitializingBean {

  private UniqueUsernameValidator uniqueUsernameValidator;
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  //  @Transactional(readOnly = false)
  public User activate(UUID uuid) {
    User user = userRepository.getByUuid(uuid);
    user.setEnabled(true);
    user = userRepository.save(user);
    return user;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.uniqueUsernameValidator = new UniqueUsernameValidator(this);
  }

  @Override
  public User createAdminUser() {
    User user = userRepository.create();
    user.getRoles().add(Role.ADMIN);
    return user;
  }

  @Override
  //  @Transactional(readOnly = false)
  public User deactivate(UUID uuid) {
    User user = userRepository.getByUuid(uuid);
    user.setEnabled(false);
    user = userRepository.save(user);
    return user;
  }

  @Override
  public boolean doesActiveAdminUserExist() {
    List findActiveAdminUsers = userRepository.getActiveAdminUsers();
    if (findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return userRepository.find(pageRequest);
  }

  @Override
  //  @Transactional(readOnly = true)
  public List<User> getActiveAdminUsers() {
    return userRepository.getActiveAdminUsers();
  }

  @Override
  public User getByUuid(UUID uuid) {
    return userRepository.getByUuid(uuid);
  }

  /*
   see: http://stackoverflow.com/questions/19302196/transaction-marked-as-rollback-only-how-do-i-find-the-cause
   When you mark your method as @Transactional, occurrence of any exception inside your method will mark the surrounding TX as roll-back only (even if you catch them). You can use other attributes of
   @Transactional annotation to prevent it of rolling back like:

   @Transactional(rollbackFor=MyException.class, noRollbackFor=MyException2.class)
  */
  @Override
  //  @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
  public User getByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.getByEmail(username);
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    return user;
  }

  @Override
  //  @Transactional(readOnly = false)
  public User save(User user, Errors results) {
    uniqueUsernameValidator.validate(user, results);
    if (!results.hasErrors()) {
      return userRepository.save(user);
    }
    return null;
  }

  private void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "email");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public User update(User user, Errors results) {
    return userRepository.update(user);
  }
}
