package de.digitalcollections.cudami.admin.business.impl.service.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.business.impl.validator.PasswordsValidatorParams;
import de.digitalcollections.cudami.admin.business.impl.validator.UniqueUsernameValidator;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.security.CudamiUsersClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
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
public class UserServiceImpl implements UserService<User>, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final CudamiUsersClient client;
  private final MessageSource messageSource;
  private final Validator passwordsValidator;
  private Validator uniqueUsernameValidator;

  public UserServiceImpl(
      @Qualifier("passwordsValidator") Validator passwordsValidator,
      CudamiClient client,
      MessageSource messageSource) {
    this.passwordsValidator = passwordsValidator;
    this.client = client.forUsers();
    this.messageSource = messageSource;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.uniqueUsernameValidator = new UniqueUsernameValidator(messageSource, this);
  }

  private org.springframework.security.core.userdetails.User buildUserForAuthentication(
      User user, List<? extends GrantedAuthority> authorities) {
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPasswordHash(), user.isEnabled(), true, true, true, authorities);
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public User create() {
    return client.create();
  }

  @Override
  public User create(User user, String password1, String password2, Errors results)
      throws ServiceException {
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
  public boolean doesActiveAdminUserExist() throws ServiceException {
    try {
      List<User> findActiveAdminUsers = client.getActiveAdminUsers();
      if (findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty()) {
        return true;
      }
      return false;
    } catch (TechnicalException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) throws ServiceException {
    try {
      return client.find(pageRequest);
    } catch (TechnicalException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public List<User> findAll() throws ServiceException {
    try {
      return client.getAll();
    } catch (TechnicalException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public User getByEmail(String email) throws ServiceException {
    try {
      return client.getByEmail(email);
    } catch (TechnicalException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public User getByUuid(UUID uuid) throws ServiceException {
    try {
      return client.getByUuid(uuid);
    } catch (TechnicalException ex) {
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
      user = client.getByEmail(username);
    } catch (ResourceNotFoundException | TechnicalException ex) {
      throw new UsernameNotFoundException(
          String.format("User \"%s\" was not found.", username), ex);
    }
    if (user == null || !user.isEnabled()) {
      throw new UsernameNotFoundException(String.format("User \"%s\" was not found.", username));
    }
    List<? extends GrantedAuthority> authorities = user.getRoles();

    return buildUserForAuthentication(user, authorities);
  }

  // TODO: Simplify user management
  private User save(String password1, String password2, User user, Errors results, boolean isUpdate)
      throws ServiceException {
    final PasswordsValidatorParams passwordsValidatorParams =
        new PasswordsValidatorParams(password1, password2, user.getPasswordHash());
    String password = passwordsValidatorParams.getPassword1();
    if (StringUtils.hasText(password)) {
      passwordsValidator.validate(passwordsValidatorParams, results);
    }
    if (!results.hasErrors()) {
      if (StringUtils.hasText(password)) {
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
      } catch (TechnicalException ex) {
        throw new ServiceException(ex.getMessage(), ex);
      }
    }
    return user;
  }

  @Override
  public boolean setStatus(UUID uuid, boolean enabled) {
    try {
      User user = client.getByUuid(uuid);
      user.setEnabled(enabled);
      client.update(user.getUuid(), user);
      return true;
    } catch (TechnicalException ex) {
      return false;
    }
  }

  // TODO: Simplify user management
  @Override
  public User update(User user, String password1, String password2, Errors results)
      throws ServiceException {
    return save(password1, password2, user, results, true);
  }
}
