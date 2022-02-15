package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierTypeRepositoryImpl.SQL_INSERT_FIELDS;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierTypeRepositoryImpl.SQL_INSERT_VALUES;
import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends JdbiRepositoryImpl implements UserRepository {

  public static final String MAPPING_PREFIX = "u";
  public static final String SQL_INSERT_FIELDS =
      " uuid, acronym, created, label, last_modified, url";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :acronym, :created, :label::JSONB, :lastModified, :url";
  public static final String SQL_REDUCED_FIELDS_US =
      " u.uuid, u.created, u.email, u.enabled, u.firstname, u.lastname, u.last_modified, u.passwordhash, u.roles";
  public static final String SQL_FULL_FIELDS_US = SQL_REDUCED_FIELDS_US;
  public static final String TABLE_ALIAS = "u";
  public static final String TABLE_NAME = "users";

  @Autowired
  public UserRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public User create() {
    return new User();
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT " + SQL_REDUCED_FIELDS_US + " FROM users");

    addPageRequestParams(pageRequest, query);
    List<User> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString()).mapToBean(User.class).map(User.class::cast).list());
    long total = count();
    PageResponse<User> pageResponse = new PageResponse<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<User> findActiveAdminUsers() {
    return dbi.withHandle(
        h ->
            h.createQuery(
                    "SELECT "
                        + SQL_REDUCED_FIELDS_US
                        + " FROM users WHERE '"
                        + Role.ADMIN.name()
                        + "' = any(roles)")
                .mapToBean(User.class)
                .map(User.class::cast)
                .list());
  }

  @Override
  public User findByEmail(String email) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery("SELECT " + SQL_FULL_FIELDS_US + " FROM users WHERE email = :email")
                    .bind("email", email)
                    .mapToBean(User.class)
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
                h.createQuery("SELECT " + SQL_FULL_FIELDS_US + " FROM users WHERE uuid = :uuid")
                    .bind("uuid", uuid)
                    .mapToBean(User.class)
                    .map(User.class::cast)
                    .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("email", "firstname", "lastname"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "email":
        return tableAlias + ".email";
      case "lastname":
        return tableAlias + ".lastname";
      case "firstname":
        return tableAlias + ".firstname";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  public User save(User user) {
    user.setUuid(UUID.randomUUID());
    user.setCreated(LocalDateTime.now());
    user.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ")"
            + " VALUES ("
            + SQL_INSERT_VALUES
            + ")"
            + " RETURNING *";

    User result =
        dbi.withHandle(
            h ->
                h.registerArrayType(Role.class, "varchar")
                    .createQuery(sql)
                    .bindBean(user)
                    .mapToBean(User.class)
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
                    .mapToBean(User.class)
                    .map(User.class::cast)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
