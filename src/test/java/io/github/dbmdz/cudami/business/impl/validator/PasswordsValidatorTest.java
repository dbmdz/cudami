package io.github.dbmdz.cudami.business.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class PasswordsValidatorTest {

  MessageSource messageSource = Mockito.mock(MessageSource.class);

  @BeforeEach
  public void setup() throws Exception {
    Mockito.when(messageSource.getMessage(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("foobar");
  }

  @Test
  public void testContainsSpecialCharFalse() {
    String password = "abcdef12345ABC";
    PasswordsValidator instance = new PasswordsValidator(messageSource);
    boolean expResult = false;
    boolean result = instance.containsSpecialChar(password);
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testContainsSpecialCharTrue() {
    String password = "aB923$";
    PasswordsValidator instance = new PasswordsValidator(messageSource);
    boolean expResult = true;
    boolean result = instance.containsSpecialChar(password);
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testValidateMinimumPasswordLengthFalse() {
    String password = "Hopfen";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator(messageSource);
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
    PasswordsValidator instance = new PasswordsValidator(messageSource);
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
    PasswordsValidator instance = new PasswordsValidator(messageSource);
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    assertThat(errors.getErrorCount()).isZero();
  }
}
