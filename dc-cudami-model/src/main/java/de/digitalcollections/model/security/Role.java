package de.digitalcollections.model.security;

import org.springframework.security.core.GrantedAuthority;

/** A user's role. */
public enum Role implements GrantedAuthority {
  ADMIN,
  CONTENT_MANAGER;

  /** Prefix needed by Spring Security */
  public static final String PREFIX = "ROLE_";

  @Override
  public String getAuthority() {
    return PREFIX + name();
  }
}
