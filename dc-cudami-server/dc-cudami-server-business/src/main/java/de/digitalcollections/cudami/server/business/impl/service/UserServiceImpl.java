package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.cudami.server.backend.api.repository.UserRepository;
import de.digitalcollections.cudami.server.business.api.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Service for User handling.
 */
@Service
//@Transactional(readOnly = true)
public class UserServiceImpl implements UserService<User> {

  @Autowired
  @Qualifier("uniqueUsernameValidator")
  private Validator uniqueUsernameValidator;

  @Autowired
  private UserRepository userRepository;

  @Override
  //  @Transactional(readOnly = false)
  public User activate(UUID uuid) {
    User user = (User) userRepository.findOne(uuid);
    user.setEnabled(true);
    user = userRepository.save(user);
    return user;
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) {
    return userRepository.find(pageRequest);
  }

  @Override
  //  @Transactional(readOnly = false)
  public User save(User user, Errors results) {
    uniqueUsernameValidator.validate(user, results);
    if (!results.hasErrors()) {
      return (User) userRepository.save(user);
    }
    return null;
  }

  @Override
  public User createAdminUser() {
    UserImpl user = (UserImpl) userRepository.create();
    user.getRoles().add(Role.ADMIN);
    return user;
  }

  @Override
  //  @Transactional(readOnly = false)
  public User deactivate(UUID uuid) {
    User user = (User) userRepository.findOne(uuid);
    user.setEnabled(false);
    user = userRepository.save(user);
    return user;
  }

  @Override
  public boolean doesActiveAdminUserExist() {
    List findActiveAdminUsers = userRepository.findActiveAdminUsers();
    if (findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  public User get(UUID uuid) {
    return (User) userRepository.findOne(uuid);
  }

  /*
     see: http://stackoverflow.com/questions/19302196/transaction-marked-as-rollback-only-how-do-i-find-the-cause
     When you mark your method as @Transactional, occurrence of any exception inside your method will mark the surrounding TX as roll-back only (even if you catch them). You can use other attributes of
     @Transactional annotation to prevent it of rolling back like:

     @Transactional(rollbackFor=MyException.class, noRollbackFor=MyException2.class)
   */
  @Override
  //  @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
  public User loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username);
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    return user;
  }

  @Override
  //  @Transactional(readOnly = false)
  public User update(User user, Errors results) {
    return (User) userRepository.update(user);
  }

  @Override
  //  @Transactional(readOnly = true)
  public List<User> findActiveAdminUsers() {
    return userRepository.findActiveAdminUsers();
  }
}
