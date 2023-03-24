package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class EntityRelationMapper<E extends Entity> {

  private final EntityRepository<E> entityRepository;

  public EntityRelationMapper(EntityRepository<E> entityRepository) {
    this.entityRepository = entityRepository;
  }

  public RowMapper<EntityToEntityRelation> getMapper(final E subjectEntity) {
    return (ResultSet rs, StatementContext ctx) -> {
      String subjectUuid = rs.getString("rel_subject");
      String predicate = rs.getString("rel_predicate");
      String objectUuid = rs.getString("rel_object");
      String[] additionalPredicates =
          rs.getArray("rel_addpredicates") != null
              ? (String[]) rs.getArray("rel_addpredicates").getArray()
              : new String[0];

      Entity subject = subjectEntity;
      if (subjectEntity == null) {
        subject = entityRepository.getByUuid(UUID.fromString(subjectUuid));
      }
      Entity object = entityRepository.getByUuid(UUID.fromString(objectUuid));

      EntityToEntityRelation result = new EntityToEntityRelation();

      result.setSubject(subject);
      result.setPredicate(predicate);
      result.setObject(object);
      result.setAdditionalPredicates(
          additionalPredicates.length > 0
              ? Arrays.stream(additionalPredicates).collect(Collectors.toList())
              : null);

      return result;
    };
  }
}
