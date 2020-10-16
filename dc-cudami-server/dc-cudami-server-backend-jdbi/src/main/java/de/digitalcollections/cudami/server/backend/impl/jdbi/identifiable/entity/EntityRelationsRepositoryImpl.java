package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRelationsRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityRelationImpl;
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
public class EntityRelationsRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements EntityRelationsRepository {

  private final Jdbi dbi;
  private final EntityRepositoryImpl entityRepository;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public EntityRelationsRepositoryImpl(
      DataSource dataSource,
      Jdbi dbi,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl entityRepository) {
    this.dbi = dbi;
    this.entityRepository = entityRepository;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public PageResponse<EntityRelation> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT rel.subject_uuid rel_subject, rel.predicate rel_predicate, rel.object_uuid rel_object"
                + " FROM rel_entity_entities as rel");
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

              Entity subject = entityRepository.findOne(UUID.fromString(subjectUuid));
              Entity object = entityRepository.findOne(UUID.fromString(objectUuid));

              return new EntityRelationImpl(subject, predicate, object);
            });
    String countQuery = "SELECT count(*) FROM rel_entity_entities as rel";
    if (!filterClauses.isEmpty()) {
      countQuery += " WHERE " + filterClauses;
    }
    final String sqlCount = countQuery;
    long count = dbi.withHandle(h -> h.createQuery(sqlCount).mapTo(Long.class).findOne().get());
    PageResponse<EntityRelation> pageResponse = new PageResponseImpl<>(result, pageRequest, count);
    return pageResponse;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"subject_uuid", "predicate", "object_uuid"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "predicate":
        return "rel.predicate";
      default:
        return null;
    }
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
                  "INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) "
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
