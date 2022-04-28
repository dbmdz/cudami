package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import de.digitalcollections.cudami.model.config.CudamiConfig;
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
  public static final String TABLE_ALIAS = "u";
  public static final String TABLE_NAME = "users";

  public static final String SQL_INSERT_FIELDS =
      " email, enabled, firstname, lastname, passwordhash, roles, uuid";
  public static final String SQL_INSERT_VALUES =
      " :email, :enabled, :firstname, :lastname, :passwordHash, :roles, :uuid";
  public static final String SQL_REDUCED_FIELDS_US =
      String.format(
          " %1$s.uuid, %1$s.created, %1$s.email, %1$s.enabled, %1$s.firstname, %1$s.lastname, %1$s.last_modified, %1$s.passwordhash, %1$s.roles",
          TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_US = SQL_REDUCED_FIELDS_US;

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
    // Actually "*" should be used in select, but here we don't need it as there is no outer select
    StringBuilder query =
        new StringBuilder(
            "SELECT " + SQL_REDUCED_FIELDS_US + " FROM " + tableName + " AS " + tableAlias);

    addPageRequestParams(pageRequest, query);
    List<User> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(User.class).list());
    long total = count();
    PageResponse<User> pageResponse = new PageResponse<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<User> getActiveAdminUsers() {
    return dbi.withHandle(
        h ->
            h.createQuery(
                    "SELECT "
                        + SQL_REDUCED_FIELDS_US
                        + " FROM "
                        + tableName
                        + " AS "
                        + tableAlias
                        + " WHERE '"
                        + Role.ADMIN.name()
                        + "' = any(roles)")
                .mapToBean(User.class)
                .list());
  }

  @Override
  public User getByEmail(String email) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT "
                            + SQL_FULL_FIELDS_US
                            + " FROM "
                            + tableName
                            + " AS "
                            + tableAlias
                            + " WHERE email = :email")
                    .bind("email", email)
                    .mapToBean(User.class)
                    .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  public User getByUuid(UUID uuid) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT "
                            + SQL_FULL_FIELDS_US
                            + " FROM "
                            + tableName
                            + " AS "
                            + tableAlias
                            + " WHERE uuid = :uuid")
                    .bind("uuid", uuid)
                    .mapToBean(User.class)
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
    user.setLastModified(LocalDateTime.now());
    User result =
        dbi.withHandle(
            h ->
                h.registerArrayType(Role.class, "varchar")
                    .createQuery(
                        "UPDATE "
                            + tableName
                            + " SET email=:email, enabled=:enabled, firstname=:firstname, lastname=:lastname, last_modified=:lastModified, passwordhash=:passwordHash, roles=:roles WHERE uuid=:uuid RETURNING *")
                    .bindBean(user)
                    .mapToBean(User.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
