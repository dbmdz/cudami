package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.semantic.Headword;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
public class HeadwordRepositoryImpl extends JdbiRepositoryImpl implements HeadwordRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hword";
  public static final String SQL_INSERT_FIELDS = " uuid, created, label, locale, last_modified";
  public static final String SQL_INSERT_VALUES = " :uuid, :created, :label, :locale, :lastModified";
  public static final String SQL_REDUCED_FIELDS_HW =
      " hw.uuid hword_uuid, hw.label hword_label, hw.locale hword_locale,"
          + " hw.created hword_created, hw.last_modified hword_lastModified";
  public static final String SQL_FULL_FIELDS_HW = SQL_REDUCED_FIELDS_HW;
  public static final String TABLE_ALIAS = "hw";
  public static final String TABLE_NAME = "headwords";

  @Autowired
  public HeadwordRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  public void addRelatedEntity(UUID headwordUuid, UUID entityUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "headword_entities", "headword_uuid", headwordUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO headword_entities(headword_uuid, entity_uuid, sortindex) VALUES (:headwordUuid, :entityUuid, :sortindex)")
                .bind("headwordUuid", headwordUuid)
                .bind("entityUuid", entityUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "headword_fileresources", "headword_uuid", headwordUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO headword_fileresources(headword_uuid, fileresource_uuid, sortindex) VALUES (:headwordUuid, :fileresourceUuid, :sortindex)")
                .bind("headwordUuid", headwordUuid)
                .bind("fileresourceUuid", fileResourceUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void delete(String label, Locale locale) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM " + tableName + " WHERE label = :label AND locale = :locale")
                .bind("label", label)
                .bind("locale", locale)
                .execute());
  }

  @Override
  public void delete(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // delete related data
    uuids.stream()
        .forEach(
            (u) -> {
              deleteRelatedEntities(u);
              deleteRelatedFileresources(u);
            });

    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public void deleteRelatedEntities(UUID headwordUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_entities WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public void deleteRelatedFileresources(UUID headwordUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_fileresources WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public PageResponse<Headword> find(PageRequest pageRequest) {
    return find(pageRequest, null, null);
  }

  protected PageResponse<Headword> find(
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
    List<Headword> result =
        retrieveList(SQL_REDUCED_FIELDS_HW, innerQuery, argumentMappings, orderBy);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, sqlCount, argumentMappings);
    long total = retrieveCount(sqlCount);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Headword> find(SearchPageRequest searchPageRequest) {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    String searchTerm = searchPageRequest.getQuery();
    if (!StringUtils.hasText(searchTerm)) {
      return find(searchPageRequest, commonSql, Collections.EMPTY_MAP);
    }

    commonSql += " WHERE label ILIKE :searchTerm || '%' ";
    return find(searchPageRequest, commonSql, Map.of("searchTerm", searchTerm));
  }

  protected SearchPageResponse<Headword> find(
      SearchPageRequest searchPageRequest, String commonSql, Map<String, Object> argumentMappings) {
    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".*" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);
    addPageRequestParams(searchPageRequest, innerQuery);
    String orderBy = getOrderBy(searchPageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<Headword> result =
        retrieveList(SQL_REDUCED_FIELDS_HW, innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
  }

  @Override
  public List<Headword> find(String label, Locale locale) {
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("label")
            .isEquals(label)
            .filter("locale")
            .isEquals(locale)
            .build();
    // TODO make PageRequest able to return all items (one page)
    PageRequest request =
        new PageRequest(
            0,
            10000,
            Sorting.defaultBuilder().order(new Order(Direction.ASC, "label")).build(),
            filtering);
    PageResponse<Headword> response = find(request);
    return response.getContent();
  }

  @Override
  public List<Headword> findAll() {
    return retrieveList(SQL_REDUCED_FIELDS_HW, null, null);
  }

  @Override
  public List<Headword> findByLabel(String label) {
    Filtering filtering = Filtering.defaultBuilder().filter("label").isEquals(label).build();
    // FIXME make PageRequest able to return all items (one page)
    PageRequest request =
        new PageRequest(
            0,
            10000,
            Sorting.defaultBuilder().order(new Order(Direction.ASC, "label")).build(),
            filtering);
    PageResponse<Headword> response = find(request);
    return response.getContent();
  }

  @Override
  public PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword findOne(UUID uuid, Filtering filtering) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword findOneByLabelAndLocale(String label, Locale locale) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Headword> findRandom(int count) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "locale":
        return tableAlias + ".locale";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query = "SELECT DISTINCT locale FROM " + tableName;
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Entity> getRelatedEntities(UUID headwordUuid) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<Entity> getRelatedEntities(UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID headwordUuid) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<FileResource> getRelatedFileResources(
      UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  public long retrieveCount(StringBuilder sqlCount) {
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());
    return total;
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

  public List<Headword> retrieveList(String fieldsSql, StringBuilder innerQuery, String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<Headword> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle.createQuery(sql).mapToBean(Headword.class).collect(Collectors.toList());
            });
    return result;
  }

  private List<Headword> retrieveList(
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

    List<Headword> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .mapToBean(Headword.class)
                  .collect(Collectors.toList());
            });
    return result;
  }

  @Override
  public Headword save(Headword headword) {
    if (headword.getUuid() == null) {
      headword.setUuid(UUID.randomUUID());
    }
    headword.setCreated(LocalDateTime.now());
    headword.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ") VALUES ("
            + SQL_INSERT_VALUES
            + ")";

    dbi.withHandle(h -> h.createUpdate(sql).bindBean(headword).execute());

    return headword;
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID headwordUuid, List<Entity> entities) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID headwordUuid, List<FileResource> fileResources) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword update(Headword headword) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
