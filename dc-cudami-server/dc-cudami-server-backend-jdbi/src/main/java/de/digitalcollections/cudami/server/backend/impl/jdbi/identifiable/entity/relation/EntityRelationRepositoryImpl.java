package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
// TODO: added Spring JDBC-Template framework, because I couldn't get JDBI get to work with double
// join on entities... sorry. Solutions welcome
public class EntityRelationRepositoryImpl extends JdbiRepositoryImpl
    implements EntityRelationRepository {

  public static final String MAPPING_PREFIX = "rel";
  public static final String TABLE_ALIAS = "rel";
  public static final String TABLE_NAME = "rel_entity_entities";

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public EntityRelationRepositoryImpl(
      DataSource dataSource,
      Jdbi dbi,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
    this.entityRepositoryImpl = entityRepositoryImpl;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO "
                        + tableName
                        + "(subject_uuid, predicate, object_uuid) VALUES (:subject_uuid, :predicate, :object_uuid)")
                .bind("subject_uuid", subjectEntityUuid)
                .bind("predicate", predicate)
                .bind("object_uuid", objectEntityUuid)
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
  public PageResponse<EntityRelation> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT rel.subject_uuid rel_subject, rel.predicate rel_predicate, rel.object_uuid rel_object"
                + " FROM "
                + tableName
                + " AS "
                + tableAlias);
    // handle optional filtering params
    String filterClauses = getFilterClauses(pageRequest.getFiltering());
    if (!filterClauses.isEmpty()) {
      query.append(" WHERE ").append(filterClauses);
    }
    addPageRequestParams(pageRequest, query);

    List<EntityRelation> result =
        jdbcTemplate.query(
            query.toString(),
            (rs, rowNum) -> {
              String subjectUuid = rs.getString("rel_subject");
              String predicate = rs.getString("rel_predicate");
              String objectUuid = rs.getString("rel_object");

              Entity subject = entityRepositoryImpl.findOne(UUID.fromString(subjectUuid));
              Entity object = entityRepositoryImpl.findOne(UUID.fromString(objectUuid));

              return new EntityRelationImpl(subject, predicate, object);
            });
    String countQuery = "SELECT count(*) FROM " + tableName + " AS " + tableAlias;
    if (!filterClauses.isEmpty()) {
      countQuery += " WHERE " + filterClauses;
    }
    final String sqlCount = countQuery;
    long count = dbi.withHandle(h -> h.createQuery(sqlCount).mapTo(Long.class).findOne().get());
    PageResponse<EntityRelation> pageResponse = new PageResponseImpl<>(result, pageRequest, count);
    return pageResponse;
  }

  @Override
  public List<EntityRelation> findBySubject(UUID subjectEntityUuid) {
    Entity subjectEntity = entityRepositoryImpl.findOne(subjectEntityUuid);
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
  protected String[] getAllowedOrderByFields() {
    return new String[] {"subject", "predicate", "object"};
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
  public void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO "
                        + tableName
                        + "(subject_uuid, predicate, object_uuid) VALUES (:subject_uuid, :predicate, :object_uuid)")
                .bind("subject_uuid", subjectEntityUuid)
                .bind("predicate", predicate)
                .bind("object_uuid", objectEntityUuid)
                .execute());
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
