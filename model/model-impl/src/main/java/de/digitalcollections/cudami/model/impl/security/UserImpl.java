package de.digitalcollections.cudami.model.impl.security;

import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.StringUtils;

public class UserImpl implements User<Long> {

  @NotBlank
  @Email
  private String email;

  private boolean enabled = true;

  @NotBlank
  private String firstname;

  private Long id;

  @NotBlank
  private String lastname;

  private String passwordHash;

  private List<Role> roles = new ArrayList<>();

  public UserImpl() {
  }

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
   * Sets password hash if password hash is not empty, otherwise no operation. Deleting of password is not possible.
   *
   * @param passwordHash new hashed password
   */
  @Override
  public void setPasswordHash(String passwordHash) {
    if (!StringUtils.isEmpty(passwordHash)) {
      this.passwordHash = passwordHash;
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
