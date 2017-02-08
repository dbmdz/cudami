package de.digitalcollections.cms.model.impl.security;

import de.digitalcollections.cms.model.api.security.Role;
import de.digitalcollections.cms.model.api.security.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * Sample implementation. Useful for unit tests...
 */
public class UserImpl implements User<Long> {

  private String email;
  private boolean enabled = true;
  private String firstname;
  private Long id;
  private String lastname;
  private String passwordHash;
  private List<Role> roles = new ArrayList<>(0);

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getFirstname() {
    return firstname;
  }

  @Override
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getLastname() {
    return lastname;
  }

  @Override
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  @Override
  public String getPasswordHash() {
    return passwordHash;
  }

  /**
   * Sets password hash if password is not empty, otherwise no operation. Deleting of password is not possible.
   *
   * @param password new password to be set hashed
   */
  @Override
  public void setPasswordHash(String password) {
    if (!StringUtils.isEmpty(password)) {
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      this.passwordHash = passwordEncoder.encode(password);
    }
  }

  @Override
  public List<Role> getRoles() {
    return this.roles;
  }

  @Override
  public void setRoles(List<Role> userRoles) {
    this.roles = userRoles;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
