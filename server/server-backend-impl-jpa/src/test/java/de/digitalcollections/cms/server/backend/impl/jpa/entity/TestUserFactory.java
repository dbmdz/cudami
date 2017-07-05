package de.digitalcollections.cms.server.backend.impl.jpa.entity;

public class TestUserFactory {

  public static UserImplJpa build(String email) {
    UserImplJpa user = new UserImplJpa();
    user.setEmail(email);
    return user;
  }
}
