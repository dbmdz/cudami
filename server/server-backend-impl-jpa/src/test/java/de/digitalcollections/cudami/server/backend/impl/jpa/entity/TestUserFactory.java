package de.digitalcollections.cudami.server.backend.impl.jpa.entity;

import de.digitalcollections.cudami.server.backend.impl.jpa.entity.UserImplJpa;

public class TestUserFactory {

  public static UserImplJpa build(String email) {
    UserImplJpa user = new UserImplJpa();
    user.setEmail(email);
    return user;
  }
}
