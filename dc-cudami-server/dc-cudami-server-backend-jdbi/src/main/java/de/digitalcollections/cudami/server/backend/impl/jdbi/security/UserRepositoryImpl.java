package de.digitalcollections.cudami.server.backend.impl.jdbi.security;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.security.UserRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  // FIXME: had to override because mapping_prefix usage (seems to be always empty prefix in
  // jdbi-rowmapper) in super class does not work
  @Override
  public PageResponse<User> find(PageRequest pageRequest) throws RepositoryException {
    StringBuilder commonSql = new StringBuilder(" FROM " + tableName + " AS " + tableAlias);
    Map<String, Object> argumentMappings = new HashMap<>(0);
    addFiltering(pageRequest, commonSql, argumentMappings);

    // Actually "*" should be used in select, but here we don't need it as there is no outer select
    StringBuilder query = new StringBuilder("SELECT " + getSqlSelectReducedFields() + commonSql);
    addPagingAndSorting(pageRequest, query);
    List<User> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .mapToBean(User.class)
                    .list());

    long total = count(commonSql.toString(), argumentMappings);
    return new PageResponse<>(result, pageRequest, total, null);
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
                  + " WHERE '"
                  + Role.ADMIN.name()
                  + "' = any(roles)";
          return h.createQuery(sql).mapToBean(User.class).list();
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
    List<User> users =
        dbi.withHandle(
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
                    .mapToBean(User.class)
                    .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  // FIXME: had to override because mapping_prefix usage (seems to be always empty prefix in
  // jdbi-rowmapper) in super class does not work
  @Override
  public User getByUuid(UUID uuid) {
    List<User> users =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT "
                            + getSqlSelectAllFields()
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
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    return new ArrayList<>(
        Arrays.asList(
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "email"),
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "firstname"),
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "lastname")));
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
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    //    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
    // FIXME: Why does mapping prefix not work for User? ???
    //    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
    //    + ", "
    return " "
        + tableAlias
        + ".uuid, "
        //        + mappingPrefix
        //        + "_uuid, "
        + tableAlias
        + ".created, "
        //        + mappingPrefix
        //        + "_created, "
        + tableAlias
        + ".last_modified, "
        //        + mappingPrefix
        //        + "_lastModified"
        + tableAlias
        + ".email, "
        //        + mappingPrefix
        //        + "_email, "
        + tableAlias
        + ".enabled, "
        //        + mappingPrefix
        //        + "_enabled, "
        + tableAlias
        + ".firstname, "
        //        + mappingPrefix
        //        + "_firstname, "
        + tableAlias
        + ".lastname, "
        //        + mappingPrefix
        //        + "_lastname, "
        + tableAlias
        + ".passwordhash, "
        //        + mappingPrefix
        //        + "_passwordhash, "
        + tableAlias
        + ".roles ";
    //        + mappingPrefix
    //        + "_roles";
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", email=:email, enabled=:enabled, firstname=:firstname, lastname=:lastname, passwordhash=:passwordHash, roles=:roles";
  }

  //  @Override
  //  public void save(final User user) {
  //    user.setUuid(UUID.randomUUID());
  //    user.setCreated(LocalDateTime.now());
  //    user.setLastModified(LocalDateTime.now());
  //
  //    final String sql =
  //        "INSERT INTO "
  //            + tableName
  //            + "("
  //            + getSqlInsertFields()
  //            + ")"
  //            + " VALUES ("
  //            + getSqlInsertValues()
  //            + ")"
  //            + " RETURNING *";
  //
  //    //    User result =
  //    dbi.withHandle(
  //        h ->
  //            h.registerArrayType(Role.class, "varchar")
  //                .createQuery(sql)
  //                .bindBean(user)
  //                .mapToBean(User.class)
  //                .findOne()
  //                .orElse(null));
  //  }

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

  // FIXME: had to override because mapping_prefix usage (seems to be always empty prefix in
  // jdbi-rowmapper) in super class does not work
  @Override
  public void update(User user) {
    user.setLastModified(LocalDateTime.now());
    //    User result =
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
    //    return result;
  }
}
