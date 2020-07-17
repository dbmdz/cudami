package de.digitalcollections.cudami.admin.business.impl.validator;

import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.api.security.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Validates that username does not exist, yet. */
public class UniqueUsernameValidator implements Validator {

  UserService userService;

  public UniqueUsernameValidator(UserService userService) {
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
        errors.rejectValue("email", "error.username_already_exists");
      }
    } catch (UsernameNotFoundException ex) {
      // ok, username not used, yet.
    }
  }
}
