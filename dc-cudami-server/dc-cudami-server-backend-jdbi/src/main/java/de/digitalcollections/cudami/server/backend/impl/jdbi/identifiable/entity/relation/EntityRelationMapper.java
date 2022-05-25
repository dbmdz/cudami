package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class EntityRelationMapper<E extends Entity> implements RowMapper<EntityRelation> {

  private final E subjectEntity;

  public EntityRelationMapper(E subjectEntity) {
    this.subjectEntity = subjectEntity;
  }

  @Override
  public EntityRelation map(ResultSet rs, StatementContext ctx) throws SQLException {
    ConfigRegistry config = ctx.getConfig();
    ColumnMappers columnMappers = config.get(ColumnMappers.class);
    ColumnMapper<LocalizedStructuredContent> lscMapper =
        columnMappers
            .findFor(LocalizedStructuredContent.class)
            .orElseThrow(() -> new NoSuchMapperException("LocalizedStructuredContent"));
    ColumnMapper<LocalizedText> ltMapper =
        columnMappers
            .findFor(LocalizedText.class)
            .orElseThrow(() -> new NoSuchMapperException("LocalizedText"));

    EntityRelation result = new EntityRelation();

    result.setSubject(subjectEntity);
    result.setPredicate(rs.getString("predicate"));

    Entity objectEntity = new Entity();
    objectEntity.setCreated(rs.getTimestamp("created").toLocalDateTime());
    objectEntity.setDescription(lscMapper.map(rs, "description", ctx));
    objectEntity.setIdentifiableObjectType(
        IdentifiableObjectType.valueOf(rs.getString("identifiable_objecttype")));
    objectEntity.setLabel(ltMapper.map(rs, "label", ctx));
    objectEntity.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
    //    objectEntity.setType(IdentifiableType.valueOf(rs.getString("identifiable_type"))); // set
    // in constructor
    objectEntity.setUuid(rs.getObject("uuid", UUID.class));

    result.setObject(objectEntity);
    return result;
  }
}
