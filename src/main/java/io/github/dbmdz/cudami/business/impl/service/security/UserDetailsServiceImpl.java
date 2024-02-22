package io.github.dbmdz.cudami.business.impl.service.security;

import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.api.service.security.UserService;
import io.github.dbmdz.cudami.model.security.AuthenticatedUser;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  private final UserService<User> userService;

  @SuppressFBWarnings
  public UserDetailsServiceImpl(UserService<User> userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    try {
      User user = userService.getByEmail(usernameOrEmail);
      if (user == null) {
        throw new UsernameNotFoundException("user does not exist");
      } else {
        return new AuthenticatedUser(user);
      }
    } catch (ServiceException ex) {
      LOGGER.error("Can not load user by username " + usernameOrEmail, ex);
      return null;
    }
  }
}
