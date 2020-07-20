package de.digitalcollections.cudami.admin.business.impl.service.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.business.impl.validator.PasswordsValidatorParams;
import de.digitalcollections.cudami.admin.business.impl.validator.UniqueUsernameValidator;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiUsersClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import de.digitalcollections.model.api.security.enums.Role;
import de.digitalcollections.model.impl.security.UserImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Service for User handling. */
@Service
public class UserServiceImpl implements UserService<UserImpl>, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final Validator passwordsValidator;

  private Validator uniqueUsernameValidator;

  private final CudamiUsersClient client;

  public UserServiceImpl(
      @Qualifier("passwordsValidator") Validator passwordsValidator, CudamiClient client) {
    this.passwordsValidator = passwordsValidator;
    this.client = client.forUsers();
  }

  @Override
  public UserImpl activate(UUID uuid) throws ServiceException {
    try {
      User user = client.findOne(uuid);
      user.setEnabled(true);
      user = client.update(user.getUuid(), user);
      return (UserImpl) user;
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public UserImpl create() {
    return (UserImpl) client.create();
  }

  @Override
  public UserImpl create(UserImpl user, String password1, String password2, Errors results)
      throws ServiceException {
    uniqueUsernameValidator.validate(user, results);
    if (!results.hasErrors()) {
      return (UserImpl) save(password1, password2, user, results, false);
    }
    return null;
  }

  @Override
  public UserImpl createAdminUser() {
    User user = create();
    List<Role> roles = new ArrayList<>();
    roles.add(Role.ADMIN);
    user.setRoles(roles);
    return (UserImpl) user;
  }

  @Override
  public UserImpl deactivate(UUID uuid) throws ServiceException {
    try {
      User user = client.findOne(uuid);
      user.setEnabled(false);
      user = client.update(user.getUuid(), user);
      return (UserImpl) user;
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public boolean doesActiveAdminUserExist() throws ServiceException {
    try {
      List<UserImpl> findActiveAdminUsers = client.findActiveAdminUsers();
      if (findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty()) {
        return true;
      }
      return false;
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public PageResponse<UserImpl> find(PageRequest pageRequest) throws ServiceException {
    try {
      return client.find(pageRequest);
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public List<UserImpl> findAll() throws ServiceException {
    try {
      return client.findAll();
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public UserImpl findByEmail(String email) throws ServiceException {
    try {
      return (UserImpl) client.findOneByEmail(email);
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public UserImpl findOne(UUID uuid) throws ServiceException {
    try {
      return (UserImpl) client.findOne(uuid);
    } catch (HttpException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  /*
   * see: http://stackoverflow.com/questions/19302196/transaction-marked-as-rollback-only-how-do-i-find-the-cause
   * When you mark your method as @Transactional, occurrence of any exception inside your method will mark the surrounding TX as roll-back only (even if you catch them).
   * You can use other attributes of @Transactional annotation to prevent it of rolling back like:
   *
   * @Transactional(rollbackFor=MyException.class, noRollbackFor=MyException2.class)
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user;
    try {
      user = client.findOneByEmail(username);
    } catch (HttpException ex) {
      throw new UsernameNotFoundException(
          String.format("User \"%s\" was not found.", username), ex);
    }
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    List<? extends GrantedAuthority> authorities = user.getRoles();

    return buildUserForAuthentication(user, authorities);
  }

  private org.springframework.security.core.userdetails.User buildUserForAuthentication(
      User user, List<? extends GrantedAuthority> authorities) {
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPasswordHash(), user.isEnabled(), true, true, true, authorities);
  }

  // TODO: Simplify user management
  @Override
  public UserImpl update(UserImpl user, String password1, String password2, Errors results)
      throws ServiceException {
    return (UserImpl) save(password1, password2, user, results, true);
  }

  // TODO: Simplify user management
  private User save(String password1, String password2, User user, Errors results, boolean isUpdate)
      throws ServiceException {
    final PasswordsValidatorParams passwordsValidatorParams =
        new PasswordsValidatorParams(password1, password2, user.getPasswordHash());
    String password = passwordsValidatorParams.getPassword1();
    if (!StringUtils.isEmpty(password)) {
      passwordsValidator.validate(passwordsValidatorParams, results);
    }
    if (!results.hasErrors()) {
      if (!StringUtils.isEmpty(password)) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordHash = passwordEncoder.encode(password);
        user.setPasswordHash(passwordHash);
      }
      try {
        if (isUpdate) {
          user = client.update(user.getUuid(), user);
        } else {
          user = client.save(user);
        }
      } catch (HttpException ex) {
        throw new ServiceException(ex.getMessage(), ex);
      }
    }
    return user;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.uniqueUsernameValidator = new UniqueUsernameValidator(this);
  }
}
