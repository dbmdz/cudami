package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRelationRepositoryImpl extends JdbiRepositoryImpl
    implements EntityRelationRepository {

  public static final String MAPPING_PREFIX = "rel";
  public static final String TABLE_ALIAS = "rel";
  public static final String TABLE_NAME = "rel_entity_entities";

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;

  @Autowired
  public EntityRelationRepositoryImpl(
      Jdbi dbi,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.entityRepositoryImpl = entityRepositoryImpl;
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    save(subjectEntityUuid, predicate, objectEntityUuid);
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
  public PageResponse<EntityRelation> find(PageRequest pageRequest) {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    StringBuilder query =
        new StringBuilder(
            "SELECT rel.subject_uuid rel_subject, rel.predicate rel_predicate, rel.object_uuid rel_object"
                + commonSql);
    Map<String, Object> argumentMappings = new HashMap<>();

    // handle optional filtering params
    addFiltering(pageRequest, query, argumentMappings);
    addPageRequestParams(pageRequest, query);

    List<EntityRelation> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .reduceResultSet(
                        new ArrayList<EntityRelation>(),
                        (acc, rs, ctx) -> {
                          String subjectUuid = rs.getString("rel_subject");
                          String predicate = rs.getString("rel_predicate");
                          String objectUuid = rs.getString("rel_object");

                          Entity subject =
                              entityRepositoryImpl.getByUuid(UUID.fromString(subjectUuid));
                          Entity object =
                              entityRepositoryImpl.getByUuid(UUID.fromString(objectUuid));

                          acc.add(new EntityRelation(subject, predicate, object));

                          return acc;
                        }));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery, argumentMappings);
    long count =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    PageResponse<EntityRelation> pageResponse = new PageResponse<>(result, pageRequest, count);
    return pageResponse;
  }

  @Override
  public List<EntityRelation> findBySubject(UUID subjectEntityUuid) {
    Entity subjectEntity = entityRepositoryImpl.getByUuid(subjectEntityUuid);
    if (subjectEntity == null) {
      return null;
    }
    return findBySubject(subjectEntity);
  }

  @Override
  public List<EntityRelation> findBySubject(Entity subjectEntity) {
    // query predicate and object entity (subject entity is given)
    String query =
        "SELECT rel.predicate as predicate, e.uuid as uuid, e.refid e_refId, e.created as created, e.description as description, e.identifiable_type as identifiable_type, e.label as label, e.last_modified as last_modified, e.entity_type as entity_type"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN entities e ON rel.object_uuid=e.uuid"
            + " WHERE rel.subject_uuid = :uuid";

    List<EntityRelation> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", subjectEntity.getUuid())
                    .map(new EntityRelationMapper(subjectEntity))
                    .list());
    return result;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("subject", "predicate", "object"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
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
  public void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    Entity subject = new Entity();
    subject.setUuid(subjectEntityUuid);

    Entity object = new Entity();
    object.setUuid(objectEntityUuid);

    save(List.of(new EntityRelation(subject, predicate, object)));
  }

  @Override
  public List<EntityRelation> save(List<EntityRelation> entityRelations) {
    if (entityRelations == null) {
      return null;
    }

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO "
                      + tableName
                      + "(subject_uuid, predicate, object_uuid) "
                      + "VALUES(:subjectUuid, :predicate, :objectUuid) "
                      + "ON CONFLICT ON CONSTRAINT rel_entity_entities_pkey DO NOTHING");
          for (EntityRelation relation : entityRelations) {
            preparedBatch
                .bind("subjectUuid", relation.getSubject().getUuid())
                .bind("predicate", relation.getPredicate())
                .bind("objectUuid", relation.getObject().getUuid())
                .add();
          }
          preparedBatch.execute();
        });
    return entityRelations;
  }
}
