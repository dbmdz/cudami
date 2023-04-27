package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityToEntityRelationMapper<E extends Entity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityToEntityRelationMapper.class);

  private final EntityRepository<E> entityRepository;

  public EntityToEntityRelationMapper(EntityRepository<E> entityRepository) {
    this.entityRepository = entityRepository;
  }

  public RowMapper<EntityRelation> getMapper(final E subjectEntity) {
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
        try {
          subject = entityRepository.getByUuid(UUID.fromString(subjectUuid));
        } catch (RepositoryException e) {
          LOGGER.error("can not get entity by UUID: " + subjectUuid, e);
        }
      }
      Entity object = null;
      try {
        object = entityRepository.getByUuid(UUID.fromString(objectUuid));
      } catch (RepositoryException e) {
        LOGGER.error("can not get entity by UUID: " + objectUuid, e);
      }

      EntityRelation result = new EntityRelation();

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
