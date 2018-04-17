package de.digitalcollections.cudami.admin.business.impl.service.security;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.business.impl.validator.PasswordsValidatorParams;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Service for User handling.
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService<User> {

  @Autowired
  @Qualifier("passwordsValidator")
  private Validator passwordsValidator;

  @Autowired
  @Qualifier("uniqueUsernameValidator")
  private Validator uniqueUsernameValidator;

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional(readOnly = false)
  public User activate(UUID uuid) {
    User user = (User) userRepository.findOne(uuid);
    user.setEnabled(true);
    user = (User) userRepository.update(user);
    return user;
  }

  @Override
  public User create() {
    return (User) userRepository.create();
  }

  @Override
  @Transactional(readOnly = false)
  public User create(User user, String password1, String password2, Errors results) {
    uniqueUsernameValidator.validate(user, results);
    if (!results.hasErrors()) {
      return save(password1, password2, user, results, false);
    }
    return null;
  }

  @Override
  public User createAdminUser() {
    User user = create();
    List<Role> roles = new ArrayList<>();
    roles.add(Role.ADMIN);
    user.setRoles(roles);
    return user;
  }

  @Override
  @Transactional(readOnly = false)
  public User deactivate(UUID uuid) {
    User user = (User) userRepository.findOne(uuid);
    user.setEnabled(false);
    user = (User) userRepository.update(user);
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
  public PageResponse<User> find(PageRequest pageRequest) {
    return userRepository.find(pageRequest);
  }

  @Override
  public User findOne(UUID uuid) {
    return (User) userRepository.findOne(uuid);
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  /*
   * see: http://stackoverflow.com/questions/19302196/transaction-marked-as-rollback-only-how-do-i-find-the-cause
   * When you mark your method as @Transactional, occurrence of any exception inside your method will mark the surrounding TX as roll-back only (even if you catch them).
   * You can use other attributes of @Transactional annotation to prevent it of rolling back like:
   *
   * @Transactional(rollbackFor=MyException.class, noRollbackFor=MyException2.class)
   */
  @Override
  @Transactional(readOnly = true, noRollbackFor = UsernameNotFoundException.class)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username);
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    List<? extends GrantedAuthority> authorities = user.getRoles();

    return buildUserForAuthentication(user, authorities);
  }

  private org.springframework.security.core.userdetails.User buildUserForAuthentication(User user, List<? extends GrantedAuthority> authorities) {
    return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(),
            user.isEnabled(), true, true, true, authorities);
  }

  @Override
  @Transactional(readOnly = false)
  public User update(User user, String password1, String password2, Errors results) {
    return save(password1, password2, user, results, true);
  }

  private User save(String password1, String password2, User user, Errors results, boolean isUpdate) {
    final PasswordsValidatorParams passwordsValidatorParams = new PasswordsValidatorParams(password1, password2, user
            .getPasswordHash());
    passwordsValidator.validate(passwordsValidatorParams, results);
    if (!results.hasErrors()) {
      String password = passwordsValidatorParams.getPassword1();
      if (!StringUtils.isEmpty(password)) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordHash = passwordEncoder.encode(password);
        user.setPasswordHash(passwordHash);
      }
      if (isUpdate) {
        user = (User) userRepository.update(user);
      } else {
        user = (User) userRepository.save(user);
      }
    }
    return user;
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
