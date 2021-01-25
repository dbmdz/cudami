package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import de.digitalcollections.model.api.security.enums.Role;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.security.UserImpl;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends JdbiRepositoryImpl implements UserRepository {

  public static final String MAPPING_PREFIX = "u";
  public static final String TABLE_ALIAS = "u";
  public static final String TABLE_NAME = "users";

  @Autowired
  public UserRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  public User create() {
    return new UserImpl();
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM users");

    addPageRequestParams(pageRequest, query);
    List<User> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .mapToBean(UserImpl.class)
                    .map(User.class::cast)
                    .list());
    long total = count();
    PageResponse<User> pageResponse = new PageResponseImpl<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<User> findActiveAdminUsers() {
    return dbi.withHandle(
        h ->
            h.createQuery("SELECT * FROM users WHERE '" + Role.ADMIN.name() + "' = any(roles)")
                .mapToBean(UserImpl.class)
                .map(User.class::cast)
                .list());
  }

  @Override
  public User findByEmail(String email) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery("SELECT * FROM users WHERE email = :email")
                    .bind("email", email)
                    .mapToBean(UserImpl.class)
                    .map(User.class::cast)
                    .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  public User findOne(UUID uuid) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery("SELECT * FROM users WHERE uuid = :uuid")
                    .bind("uuid", uuid)
                    .mapToBean(UserImpl.class)
                    .map(User.class::cast)
                    .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return Arrays.asList("email", "firstname", "lastname");
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "email":
        return "email";
      case "lastname":
        return "lastname";
      case "firstname":
        return "firstname";
      default:
        return null;
    }
  }

  @Override
  public User save(User user) {
    user.setUuid(UUID.randomUUID());
    //    UserImpl result = dbi.withHandle(h -> h.createQuery(
    //            "INSERT INTO users(email, enabled, firstname, lastname, passwordHash, roles)
    // VALUES (:email, :enabled, :firstname, :lastname, :passwordHash, :roles) RETURNING *")
    //            .bindBean(user)
    //            .bind("roles", user.getRoles().stream().map(Role::name).toArray(String[]::new))
    //            .mapToBean(UserImpl.class)
    //            .findOne().orElse(null));
    //    return (S) result;

    UserImpl result =
        dbi.withHandle(
            h ->
                h.registerArrayType(Role.class, "varchar")
                    .createQuery(
                        "INSERT INTO users(email, enabled, firstname, lastname, passwordHash, roles, uuid) VALUES (:email, :enabled, :firstname, :lastname, :passwordHash, :roles, :uuid) RETURNING *")
                    .bindBean(user)
                    .mapToBean(UserImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public User update(User user) {
    User result =
        dbi.withHandle(
            h ->
                h.registerArrayType(Role.class, "varchar")
                    .createQuery(
                        "UPDATE users SET email=:email, enabled=:enabled, firstname=:firstname, lastname=:lastname, passwordHash=:passwordHash, roles=:roles, uuid=:uuid WHERE uuid=:uuid RETURNING *")
                    .bindBean(user)
                    .mapToBean(UserImpl.class)
                    .map(User.class::cast)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
