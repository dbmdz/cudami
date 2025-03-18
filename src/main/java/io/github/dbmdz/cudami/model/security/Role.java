package io.github.dbmdz.cudami.model.security;

import org.springframework.security.core.GrantedAuthority;

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
