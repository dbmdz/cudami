package de.digitalcollections.cudami.server.backend.impl.jdbi.legal;

import de.digitalcollections.cudami.server.backend.api.repository.legal.LicenseRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class LicenseRepositoryImpl extends JdbiRepositoryImpl implements LicenseRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "l";
  public static final String SQL_INSERT_FIELDS =
      " uuid, acronym, created, label, last_modified, url";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :acronym, :created, :label::JSONB, :lastModified, :url";
  public static final String SQL_REDUCED_FIELDS_LI =
      " li.uuid, li.acronym, li.created, li.label, li.last_modified, li.url";
  public static final String SQL_FULL_FIELDS_LI = SQL_REDUCED_FIELDS_LI;
  public static final String TABLE_ALIAS = "li";
  public static final String TABLE_NAME = "licenses";

  @Autowired
  public LicenseRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  public void deleteByUrl(String url) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE url = :url")
                .bind("url", url)
                .execute());
  }

  @Override
  public void deleteByUuid(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  public void deleteByUuids(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
  }

  @Override
  public PageResponse<License> find(PageRequest pageRequest) {
    return find(pageRequest, null, new HashMap<>());
  }

  protected PageResponse<License> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings) {
    if (commonSql == null) {
      commonSql = " FROM " + tableName + " AS " + tableAlias;
    }

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery, argumentMappings);
    addPageRequestParams(pageRequest, innerQuery);

    String orderBy = getOrderBy(pageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<License> result =
        retrieveList(SQL_REDUCED_FIELDS_LI, innerQuery, argumentMappings, orderBy);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, sqlCount, argumentMappings);
    long total = retrieveCount(sqlCount, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<License> findAll() {
    return retrieveList(SQL_REDUCED_FIELDS_LI, null, null);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("acronym", "created", "label", "lastModified", "url"));
  }

  @Override
  public License getByUrl(URL url) {
    String query = "SELECT * FROM " + tableName + " WHERE url=:url";
    return dbi.withHandle(
        h -> h.createQuery(query).bind("url", url).mapToBean(License.class).findOne().orElse(null));
  }

  @Override
  public License getByUuid(UUID uuid) {
    String query = "SELECT * FROM " + tableName + " WHERE uuid=:uuid";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bind("uuid", uuid)
                .mapToBean(License.class)
                .findOne()
                .orElse(null));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "acronym":
        return tableAlias + ".acronym";
      case "created":
        return tableAlias + ".created";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "url":
        return tableAlias + ".url";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  public long retrieveCount(StringBuilder sqlCount, final Map<String, Object> argumentMappings) {
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(sqlCount.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    return total;
  }

  public List<License> retrieveList(String fieldsSql, StringBuilder innerQuery, String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<License> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle.createQuery(sql).mapToBean(License.class).collect(Collectors.toList());
            });
    return result;
  }

  private List<License> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      Map<String, Object> argumentMappings,
      String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<License> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .mapToBean(License.class)
                  .collect(Collectors.toList());
            });
    return result;
  }

  @Override
  public License save(License license) {
    if (license.getUuid() == null) {
      license.setUuid(UUID.randomUUID());
    }
    license.setCreated(LocalDateTime.now());
    license.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ") VALUES ("
            + SQL_INSERT_VALUES
            + ")";

    dbi.withHandle(h -> h.createUpdate(sql).bindBean(license).execute());

    return license;
  }

  @Override
  public License update(License license) {
    String query =
        "UPDATE "
            + tableName
            + " SET acronym=:acronym, label=:label::JSONB, last_modified=:lastModified, url=:url"
            + " WHERE uuid=:uuid RETURNING *";
    return dbi.withHandle(
        h ->
            h.createQuery(query).bindBean(license).mapToBean(License.class).findOne().orElse(null));
  }
}
