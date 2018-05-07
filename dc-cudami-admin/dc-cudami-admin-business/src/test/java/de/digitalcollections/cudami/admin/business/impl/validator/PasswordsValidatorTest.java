package de.digitalcollections.cudami.admin.business.impl.validator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
