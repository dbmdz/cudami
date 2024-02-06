package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E>
    implements EntityRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "e";
  public static final String TABLE_ALIAS = "e";
  public static final String TABLE_NAME = "entities";

  @Autowired
  public EntityRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Entity.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  public EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Entity> entityImplClass,
      int offsetForAlternativePaging,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        offsetForAlternativePaging,
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid)
      throws RepositoryException {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_entity_fileresources", "entity_uuid", entityUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
                .bind("entity_uuid", entityUuid)
                .bind("fileresource_uuid", fileResourceUuid)
                .bind("sortindex", nextSortIndex)
                .execute());
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(UUID entityUuid, PageRequest pageRequest)
      throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM entities e"
                + " INNER JOIN rel_entity_entities rel ON e.uuid=rel.object_uuid"
                + " WHERE rel.subject_uuid = :entityUuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("entityUuid", entityUuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder query = new StringBuilder("SELECT rel.sortindex AS idx, *" + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "idx")));
    addPagingAndSorting(pageRequest, query);
    List<Entity> list =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(Entity.class).list());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<Entity> pageResponse = new PageResponse<>(list, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      UUID entityUuid, PageRequest pageRequest) throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM FROM fileresources f"
                + " INNER JOIN rel_entity_fileresources rel ON f.uuid=rel.fileresource_uuid"
                + " WHERE rel.entitye_uuid = :entityUuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("entityUuid", entityUuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder query = new StringBuilder("SELECT rel.sortindex AS idx, *" + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "idx")));
    addPagingAndSorting(pageRequest, query);
    List<FileResource> list =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(FileResource.class).list());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<FileResource> pageResponse = new PageResponse<>(list, pageRequest, total);
    return pageResponse;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("refId"));
    return allowedOrderByFields;
  }

  @Override
  public E getByRefId(long refId) throws RepositoryException {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("refId").isEquals(refId).build())
            .build();

    return retrieveOne(getSqlSelectAllFields(), filtering, null);
  }

  @Override
  public String getColumnName(String modelProperty) {
    return switch (modelProperty) {
      case "navdate" -> tableAlias + ".navdate";
      case "refId" -> tableAlias + ".refid";
      case "name" -> isRepoForNamedEntity() ? "name" : null;
      case "nameLocalesOfOriginalScripts" ->
          isRepoForNamedEntity() ? "name_locales_original_scripts" : null;
      case "notes" -> tableAlias + ".notes";
      default -> super.getColumnName(modelProperty);
    };
  }

  @Override
  protected LinkedHashMap<String, Function<E, Optional<Object>>> getJsonbFields() {
    LinkedHashMap<String, Function<E, Optional<Object>>> jsonbFields = super.getJsonbFields();
    // An Entity might be a NamedEntity, too:
    jsonbFields.put(
        "name",
        i -> i instanceof NamedEntity ne ? Optional.ofNullable(ne.getName()) : Optional.empty());
    return jsonbFields;
  }

  @Override
  public List<E> getRandom(int count) throws RepositoryException {
    // Warning: could be very slow if random is used on tables with many million
    // records
    // see https://www.gab.lc/articles/bigdata_postgresql_order_by_random/
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM " + tableName + " ORDER BY RANDOM() LIMIT " + count);
    return retrieveList(getSqlSelectReducedFields(), innerQuery, null, null);
  }

  @Override
  protected List<String> getReturnedFieldsOnInsertUpdate() {
    var fields = super.getReturnedFieldsOnInsertUpdate();
    fields.add("refid");
    return fields;
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", custom_attrs"
        + ", navdate"
        + ", notes"
        + (isRepoForNamedEntity() ? ", name, name_locales_original_scripts, split_name" : "");
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    // refid is generated as serial, DO NOT SET!
    return super.getSqlInsertValues()
        + ", :customAttributes::JSONB"
        + ", :navDate"
        + ", :notes::JSONB"
        + (isRepoForNamedEntity()
            ? ", :name::JSONB, :nameLocalesOfOriginalScripts::varchar[], :split_name::varchar[]"
            : "");
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".custom_attrs "
        + mappingPrefix
        + "_customAttributes, "
        + tableAlias
        + ".navdate "
        + mappingPrefix
        + "_navDate, "
        + tableAlias
        + ".refid "
        + mappingPrefix
        + "_refId, "
        + tableAlias
        + ".notes "
        + mappingPrefix
        + "_notes"
        + (isRepoForNamedEntity()
            ? String.format(
                ", %1$s.name %2$s_name, %1$s.name_locales_original_scripts %2$s_nameLocalesOfOriginalScripts",
                tableAlias, mappingPrefix)
            : "");
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, identifiable_objecttype, refid
    return super.getSqlUpdateFieldValues()
        + ", custom_attrs=:customAttributes::JSONB, navdate=:navDate, notes=:notes::JSONB"
        + (isRepoForNamedEntity()
            ? ", name=:name::JSONB, name_locales_original_scripts=:nameLocalesOfOriginalScripts::varchar[], split_name=:split_name::varchar[]"
            : "");
  }

  @Override
  protected boolean hasSplitColumn(String propertyName) {
    if (isRepoForNamedEntity() && "name".equals(propertyName)) {
      return true;
    }
    return super.hasSplitColumn(propertyName);
  }

  @Override
  protected void insertUpdateCallback(E identifiable, Map<String, Object> returnedFields) {
    super.insertUpdateCallback(identifiable, returnedFields);
    identifiable.setRefId(Long.parseLong(returnedFields.getOrDefault("refid", 0).toString()));
  }

  /**
   * Specify if this repository handles an {@code Entity} subclass that implements the interface
   * {@code NamedEntity} so the additional fields are being added properly.
   */
  protected boolean isRepoForNamedEntity() {
    return NamedEntity.class.isAssignableFrom(uniqueObjectImplClass);
  }

  @Override
  public void save(
      E entity,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    if (isRepoForNamedEntity()) {
      bindings.put("split_name", splitToArray(((NamedEntity) entity).getName()));
    }
    super.save(entity, bindings, sqlModifier);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) throws RepositoryException {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entity_fileresources WHERE entity_uuid = :uuid")
                .bind("uuid", entityUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
          for (FileResource fileResource : fileResources) {
            preparedBatch
                .bind("uuid", entityUuid)
                .bind("fileResourceUuid", fileResource.getUuid())
                .bind("sortIndex", getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return findRelatedFileResources(entityUuid, new PageRequest(0, fileResources.size()))
        .getContent();
  }

  @Override
  public void update(E entity, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    update(entity, bindings, null);
  }

  @Override
  public void update(
      E entity,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    if (isRepoForNamedEntity()) {
      bindings.put("split_name", splitToArray(((NamedEntity) entity).getName()));
    }
    super.update(entity, bindings, sqlModifier);
  }
}
