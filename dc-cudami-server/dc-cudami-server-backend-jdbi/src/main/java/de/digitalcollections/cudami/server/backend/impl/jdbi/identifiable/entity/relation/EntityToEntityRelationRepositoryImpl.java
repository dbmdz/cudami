package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityToEntityRelationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityToEntityRelationRepositoryImpl extends JdbiRepositoryImpl
    implements EntityToEntityRelationRepository {

  public static final String MAPPING_PREFIX = "rel";
  public static final String TABLE_ALIAS = "rel";
  public static final String TABLE_NAME = "rel_entity_entities";

  private final EntityToEntityRelationMapper<Entity> entityRelationMapper;
  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;

  @Autowired
  public EntityToEntityRelationRepositoryImpl(
      Jdbi dbi,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.entityRepositoryImpl = entityRepositoryImpl;
    this.entityRelationMapper = new EntityToEntityRelationMapper<Entity>(entityRepositoryImpl);
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException {
    save(subjectEntityUuid, predicate, objectEntityUuid);
  }

  @Override
  public void deleteByObject(UUID objectEntityUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE object_uuid = :uuid")
                .bind("uuid", objectEntityUuid)
                .execute());
  }

  @Override
  public void deleteBySubject(UUID subjectEntityUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE subject_uuid = :uuid")
                .bind("uuid", subjectEntityUuid)
                .execute());
  }

  @Override
  public PageResponse<EntityToEntityRelation> find(PageRequest pageRequest) {
    StringBuilder commonSql = new StringBuilder(" FROM " + tableName + " AS " + tableAlias);
    Map<String, Object> argumentMappings = new HashMap<>(0);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder query =
        new StringBuilder(
            "SELECT rel.subject_uuid rel_subject, rel.predicate rel_predicate, rel.object_uuid rel_object, rel.additional_predicates rel_addpredicates"
                + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "rel.sortindex")));
    addPagingAndSorting(pageRequest, query);
    List<EntityToEntityRelation> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .map(entityRelationMapper.getMapper(null))
                    .list());

    String countQuery = "SELECT count(*)" + commonSql;
    long count =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    PageResponse<EntityToEntityRelation> pageResponse =
        new PageResponse<>(result, pageRequest, count);
    return pageResponse;
  }

  @Override
  public PageResponse<EntityToEntityRelation> findBySubject(
      UUID subjectUuid, PageRequest pageRequest) {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM " + tableName + " AS " + tableAlias + " WHERE subject_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", subjectUuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    // query predicate and object entity (subject entity is given)
    StringBuilder query =
        new StringBuilder(
            "SELECT rel.subject_uuid rel_subject, rel.predicate rel_predicate, rel.object_uuid rel_object, rel.additional_predicates rel_addpredicates"
                + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "rel.sortindex")));
    addPagingAndSorting(pageRequest, query);
    List<EntityToEntityRelation> list =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .bind("uuid", subjectUuid)
                    .map(entityRelationMapper.getMapper(null))
                    .list());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<EntityToEntityRelation> pageResponse =
        new PageResponse<>(list, pageRequest, total);
    return pageResponse;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("subject", "predicate", "object"));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "object":
        return tableAlias + ".object_uuid";
      case "predicate":
        return tableAlias + ".predicate";
      case "subject":
        return tableAlias + ".subject_uuid";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return null;
  }

  @Override
  public void save(List<EntityToEntityRelation> entityRelations) throws RepositoryException {
    if (entityRelations == null) {
      return;
    }

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO "
                      + tableName
                      + "(subject_uuid, predicate, object_uuid, additional_predicates, sortindex) "
                      + "VALUES(:subjectUuid, :predicate, :objectUuid, :additional_predicates, :sortindex) "
                      + "ON CONFLICT (subject_uuid, predicate, object_uuid) DO UPDATE SET "
                      + "additional_predicates = EXCLUDED.additional_predicates, "
                      + "sortindex = EXCLUDED.sortindex");
          for (int i = 0; i < entityRelations.size(); i++) {
            var relation = entityRelations.get(i);
            preparedBatch
                .bind("subjectUuid", relation.getSubject().getUuid())
                .bind("predicate", relation.getPredicate())
                .bind("objectUuid", relation.getObject().getUuid())
                .bindByType(
                    "additional_predicates",
                    relation.getAdditionalPredicates(),
                    new GenericType<List<String>>() {})
                .bind("sortindex", i)
                .add();
          }
          preparedBatch.execute();
        });
  }

  @Override
  public void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException {
    Entity subject = new Entity();
    subject.setUuid(subjectEntityUuid);

    Entity object = new Entity();
    object.setUuid(objectEntityUuid);

    save(List.of(new EntityToEntityRelation(subject, predicate, object)));
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }
}
