package de.digitalcollections.cudami.client.business.impl.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates that given passwords are equals.
 */
@Component
public class PasswordsValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return PasswordsValidatorParams.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    PasswordsValidatorParams passwords = (PasswordsValidatorParams) target;

    String password1 = passwords.getPassword1();
    String password2 = passwords.getPassword2();

    if (!ObjectUtils.nullSafeEquals(password1, password2)) {
      errors.reject("error.passwords_must_be_equals");
      return;
    }

    String passwordHash = passwords.getPasswordHash();
    if (StringUtils.isEmpty(passwordHash) && StringUtils.isEmpty(password1)) {
      errors.reject("error.passwords_must_be_filled");
      return;
    }

    if (!StringUtils.isEmpty(password1) && password1.length() < 6) {
      errors.reject("error.password_min_length", new Object[]{6}, "Password's minimum length is 6.");
      return;
    }
  }
}
