package de.digitalcollections.cms.server.backend.impl.jpa.entity;

import de.digitalcollections.cms.model.api.security.Role;
import de.digitalcollections.cms.model.api.security.User;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "users")
public class UserImplJpa implements User<Long> {

  @NotEmpty
  @Column(name = "email", nullable = false, unique = true)
  @Email
  private String email;

  @Column(name = "enabled")
  private boolean enabled = true;

  @Size(min = 3, max = 255)
  @Column(name = "firstname")
  private String firstname;

  @Id
  @TableGenerator(
          name = SequenceConstants.GENERATOR_NAME, table = SequenceConstants.TABLE_NAME,
          pkColumnName = SequenceConstants.PK_COLUMN_NAME, valueColumnName = SequenceConstants.VALUE_COLUMN_NAME,
          allocationSize = SequenceConstants.ALLOCATION_SIZE,
          pkColumnValue = "USER_SEQ"
  )
  @GeneratedValue(strategy = GenerationType.TABLE, generator = SequenceConstants.GENERATOR_NAME)
  @Column(name = "id")
  private Long id;

  @Size(min = 3, max = 255)
  @Column(name = "lastname")
  private String lastname;

  // This stores the password hash, *never* the actual password!
  @Column(name = "password")
  private String passwordHash;

  // Because of "detached entity passed to persist": CascadeType.ALL switched to "MERGE"
  // and because we never want to save Role-entity as this is a immutable/"constant" value.
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, targetEntity = RoleImplJpa.class)
  @JoinTable(name = "user_role",
          joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false)
          },
          inverseJoinColumns = {
            @JoinColumn(name = "role_id", nullable = false, updatable = false)
          }
  )
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
