package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends UniqueObjectRepositoryImpl<User> implements UserRepository {

  public static final String MAPPING_PREFIX = "us";
  public static final String TABLE_ALIAS = "u";
  public static final String TABLE_NAME = "users";

  public UserRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        User.class,
        cudamiConfig.getOffsetForAlternativePaging());

    this.dbi.registerArrayType(Role.class, "varchar");
  }

  @Override
  public User create() {
    return new User();
  }

  @Override
  public List<User> getActiveAdminUsers() {
    return dbi.withHandle(
        h -> {
          String sql =
              "SELECT "
                  + getSqlSelectReducedFields()
                  + " FROM "
                  + tableName
                  + " AS "
                  + tableAlias
                  + " WHERE ? = any(roles)";
          return h.createQuery(sql).bind(0, Role.ADMIN.name()).mapTo(User.class).list();
        });
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("email", "firstname", "lastname"));
    return allowedOrderByFields;
  }

  @Override
  public User getByEmail(String email) {
    return dbi.withHandle(
        h ->
            h.createQuery(
                    "SELECT "
                        + getSqlSelectAllFields()
                        + " FROM "
                        + tableName
                        + " AS "
                        + tableAlias
                        + " WHERE email = :email")
                .bind("email", email)
                .mapTo(User.class)
                .findFirst()
                .orElse(null));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "email":
        return tableAlias + ".email";
      case "lastname":
        return tableAlias + ".lastname";
      case "firstname":
        return tableAlias + ".firstname";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<User> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", email, enabled, firstname, lastname, passwordhash, roles";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :email, :enabled, :firstname, :lastname, :passwordHash, :roles";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
        , {{tableAlias}}.email {{mappingPrefix}}_email,
        {{tableAlias}}.enabled {{mappingPrefix}}_enabled,
        {{tableAlias}}.firstname {{mappingPrefix}}_firstname,
        {{tableAlias}}.lastname {{mappingPrefix}}_lastname,
        {{tableAlias}}.passwordhash {{mappingPrefix}}_passwordhash,
        {{tableAlias}}.roles {{mappingPrefix}}_roles """
            .replace("{{tableAlias}}", tableAlias)
            .replace("{{mappingPrefix}}", mappingPrefix);
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", email=:email, enabled=:enabled, firstname=:firstname, lastname=:lastname, passwordhash=:passwordHash, roles=:roles";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "firstname":
      case "lastname":
        return true;
      default:
        return false;
    }
  }
}
