package de.digitalcollections.cudami.admin.business.impl.validator;

/** Container for params of PasswordsValidator. */
public class PasswordsValidatorParams {

  private final String password1;
  private final String password2;
  private final String passwordHash;

  public PasswordsValidatorParams(String password1, String password2, String passwordHash) {
    this.password1 = password1;
    this.password2 = password2;
    this.passwordHash = passwordHash;
  }

  public String getPassword1() {
    return password1;
  }

  public String getPassword2() {
    return password2;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
