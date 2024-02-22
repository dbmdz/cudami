package io.github.dbmdz.cudami.business.impl.service.security;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.security.CudamiUsersClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.api.service.security.UserService;
import io.github.dbmdz.cudami.business.impl.validator.PasswordsValidatorParams;
import io.github.dbmdz.cudami.business.impl.validator.UniqueUsernameValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
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
  public void afterPropertiesSet() {
    this.uniqueUsernameValidator = new UniqueUsernameValidator(messageSource, this);
  }

  private org.springframework.security.core.userdetails.User buildUserForAuthentication(
      User user, List<? extends GrantedAuthority> authorities) {
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPasswordHash(), user.isEnabled(), true, true, true, authorities);
  }

  @Override
  public long count() throws ServiceException {
    try {
      return client.count();
    } catch (TechnicalException ex) {
      throw new ServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  public User create() throws ServiceException {
    try {
      return client.create();
    } catch (TechnicalException e) {
      throw new ServiceException("Cannot create a user: " + e, e);
    }
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
  public User createAdminUser() throws ServiceException {
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
      return findActiveAdminUsers != null && !findActiveAdminUsers.isEmpty();
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
