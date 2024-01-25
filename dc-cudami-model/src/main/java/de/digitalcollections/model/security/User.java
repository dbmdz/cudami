package de.digitalcollections.model.security;

import de.digitalcollections.model.UniqueObject;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;

/** An user of the system. */
@SuperBuilder(buildMethodName = "prebuild")
public class User extends UniqueObject {

  @NotBlank @Email private String email;

  private boolean enabled;

  @NotBlank private String firstname;

  @NotBlank private String lastname;

  private String passwordHash;

  private List<Role> roles;

  public User() {
    super();
  }

  public String getEmail() {
    return email;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public List<Role> getRoles() {
    return this.roles;
  }

  @Override
  protected void init() {
    super.init();
    enabled = true;
    roles = new ArrayList<>(0);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * Sets password hash if password hash is not empty, otherwise no operation. Deleting of password
   * is not possible.
   *
   * @param passwordHash new hashed password
   */
  public void setPasswordHash(String passwordHash) {
    if (!ObjectUtils.isEmpty(passwordHash)) {
      this.passwordHash = passwordHash;
    }
  }

  public void setRoles(List<Role> userRoles) {
    this.roles = userRoles;
  }
}
