package de.digitalcollections.cudami.admin.business.impl.validator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordsValidatorTest {

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testContainsSpecialCharFalse() {
    String password = "abcdef12345ABC";
    PasswordsValidator instance = new PasswordsValidator();
    boolean expResult = false;
    boolean result = instance.containsSpecialChar(password);
    assertEquals(expResult, result);
  }

  @Test
  public void testContainsSpecialCharTrue() {
    String password = "aB923$";
    PasswordsValidator instance = new PasswordsValidator();
    boolean expResult = true;
    boolean result = instance.containsSpecialChar(password);
    assertEquals(expResult, result);
  }

  @Test
  public void testValidateMinimumPasswordLengthFalse() {
    String password = "Hopfen";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    String expectedErrorCode = "error.password_min_length";
    String result = errors.getGlobalErrors().get(0).getCodes()[1];

    assertEquals(errors.getErrorCount(), 1);
    assertEquals(expectedErrorCode, result);
  }

  @Test
  public void testValidateMaximumPasswordLengthFalse() {
    String password = "Hopfenzeitung089Hopfenzeitung089";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    String expectedErrorCode = "error.password_max_length";
    String result = errors.getGlobalErrors().get(0).getCodes()[1];

    assertEquals(errors.getErrorCount(), 1);
    assertEquals(expectedErrorCode, result);
  }

  @Test
  public void testValidateTrue() {
    String password = "Hopfenzeitung089!";
    PasswordsValidatorParams params = new PasswordsValidatorParams(password, password, "hash");
    PasswordsValidator instance = new PasswordsValidator();
    Errors errors = new BeanPropertyBindingResult(params, "params");

    instance.validate(params, errors);

    assertEquals(errors.getErrorCount(), 0);
  }
}
