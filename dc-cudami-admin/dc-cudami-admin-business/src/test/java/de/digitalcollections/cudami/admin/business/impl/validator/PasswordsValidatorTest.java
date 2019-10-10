package de.digitalcollections.cudami.admin.business.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class PasswordsValidatorTest {

  @Test
  public void testContainsSpecialCharFalse() {
    String password = "abcdef12345ABC";
    PasswordsValidator instance = new PasswordsValidator();
    boolean expResult = false;
    boolean result = instance.containsSpecialChar(password);
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testContainsSpecialCharTrue() {
    String password = "aB923$";
    PasswordsValidator instance = new PasswordsValidator();
    boolean expResult = true;
    boolean result = instance.containsSpecialChar(password);
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testValidateMinimumPasswordLengthFalse() {
    String password = "Hopfen";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);

    String expectedErrorCode = "error.password_min_length";
    String result = errors.getGlobalErrors().get(0).getCodes()[1];

    assertThat(result).isEqualTo(expectedErrorCode);
  }

  @Test
  public void testValidateMaximumPasswordLengthFalse() {
    String password = "Hopfenzeitung089Hopfenzeitung089";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);

    String expectedErrorCode = "error.password_max_length";
    String result = errors.getGlobalErrors().get(0).getCodes()[1];

    assertThat(result).isEqualTo(expectedErrorCode);
  }

  @Test
  public void testValidateTrue() {
    String password = "Hopfenzeitung089!";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    assertThat(errors.getErrorCount()).isZero();
  }
}
