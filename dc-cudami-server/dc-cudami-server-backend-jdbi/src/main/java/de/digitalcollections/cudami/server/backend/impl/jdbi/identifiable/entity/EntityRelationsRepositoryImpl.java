package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRelationsRepository;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRelationsRepositoryImpl implements EntityRelationsRepository {

  private final Jdbi dbi;

  @Autowired
  public EntityRelationsRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
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
