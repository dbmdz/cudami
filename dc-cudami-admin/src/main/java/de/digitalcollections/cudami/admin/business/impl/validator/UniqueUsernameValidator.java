package de.digitalcollections.cudami.admin.business.impl.validator;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Validates that username does not exist, yet. */
public class UniqueUsernameValidator implements Validator {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(UniqueUsernameValidator.class);

  private final MessageSource messageSource;
  private final UserService<User> userService;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UniqueUsernameValidator(MessageSource messageSource, UserService<User> userService) {
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
      User existingUser = userService.getByEmail(user.getEmail());
      if (existingUser != null) {
        String code = "error.username_already_exists";
        errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      }
    } catch (UsernameNotFoundException ex) {
      // ok, username not used, yet.
    } catch (ServiceException ex) {
      LOGGER.error("Can not validate / get user", ex);
      errors.reject(
          "error.technical_error",
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale()));
    }
  }
}
