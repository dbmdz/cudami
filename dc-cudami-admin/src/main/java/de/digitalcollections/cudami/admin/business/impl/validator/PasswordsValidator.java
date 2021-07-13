package de.digitalcollections.cudami.admin.business.impl.validator;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Validates that given passwords are equals. */
@Component
public class PasswordsValidator implements Validator {

  private static final Pattern PATTERN_SPECIAL_CHAR = Pattern.compile(".*[^a-zA-Z0-9]+.*");
  private static final int PASSWORD_MIN_LENGTH = 12;
  private static final int PASSWORD_MAX_LENGTH = 30;

  private final MessageSource messageSource;

  public PasswordsValidator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  protected boolean containsSpecialChar(String password) {
    Matcher m = PATTERN_SPECIAL_CHAR.matcher(password);
    return m.matches();
  }

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
      String code = "error.passwords_must_be_equals";
      errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      return;
    }

    String passwordHash = passwords.getPasswordHash();
    if (!StringUtils.hasText(passwordHash) && !StringUtils.hasText(password1)) {
      String code = "error.passwords_must_be_filled";
      errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      return;
    }

    if (StringUtils.hasText(password1) && password1.length() < PASSWORD_MIN_LENGTH) {
      String code = "error.password_min_length";
      Object[] args = new Object[] {PASSWORD_MIN_LENGTH};
      errors.reject(
          code, args, messageSource.getMessage(code, args, LocaleContextHolder.getLocale()));
      return;
    }

    if (StringUtils.hasText(password1) && password1.length() > PASSWORD_MAX_LENGTH) {
      String code = "error.password_max_length";
      Object[] args = new Object[] {PASSWORD_MAX_LENGTH};
      errors.reject(
          code, args, messageSource.getMessage(code, args, LocaleContextHolder.getLocale()));
      return;
    }

    final PasswordValidator validator =
        new PasswordValidator(
            Arrays.asList(
                // Password minimum and maximum length range:
                new LengthRule(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH),
                // at least one upper case letter:
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower case letter:
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character:
                new CharacterRule(EnglishCharacterData.Digit, 1)));
    // at least one symbol (special character):
    //            new CharacterRule(EnglishCharacterData.Special, 1);
    // rejects passwords that contain a sequence of 3 digits (e.g. 123):
    //            new IllegalSequenceRule(EnglishSequenceData.Numerical, 3, false),
    // rejects passwords that contain a sequence of 5 characters (e.g. "start"):
    //            new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
    // rejects passwords that contain a sequence of 5 line-up characters on keyboard (e.g. "qwert"):
    //            new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false),
    // no whitespace characters:
    //            new WhitespaceRule()));
    final RuleResult result = validator.validate(new PasswordData(password1));
    if (!result.isValid()) {
      String code = "error.password_too_weak";
      errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      return;
    }

    // at least one special character
    if (!containsSpecialChar(password1)) {
      String code = "error.password_too_weak";
      errors.reject(code, messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
      return;
    }
  }
}
