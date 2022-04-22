package de.digitalcollections.cudami.admin.business.impl.validator;

import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Validates that username does not exist, yet. */
public class UniqueUsernameValidator implements Validator {

  private final MessageSource messageSource;
  private final UserService userService;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UniqueUsernameValidator(MessageSource messageSource, UserService userService) {
    this.messageSource = messageSource;
    this.userService = userService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return User.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    User user = (User) target;
    try {
      UserDetails existingUser = userService.loadUserByUsername(user.getEmail());
      if (existingUser != null) {
        String code = "error.username_already_exists";
        errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      }
    } catch (UsernameNotFoundException ex) {
      // ok, username not used, yet.
    }
  }
}
