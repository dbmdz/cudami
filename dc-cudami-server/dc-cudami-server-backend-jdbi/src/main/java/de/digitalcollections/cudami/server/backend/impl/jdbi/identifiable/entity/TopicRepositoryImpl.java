package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.SubtopicRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<Topic> implements TopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "to";
  public static final String TABLE_ALIAS = "t";
  public static final String TABLE_NAME = "topics";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix);
  }

  private final SubtopicRepositoryImpl subtopicRepositoryImpl;

  @Autowired
  public TopicRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      SubtopicRepositoryImpl subtopicRepositoryImpl) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, TopicImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
    this.subtopicRepositoryImpl = subtopicRepositoryImpl;
  }

  @Override
  public Topic findOne(UUID uuid, Filtering filtering) {
    Topic topic = super.findOne(uuid, filtering);

    if (topic != null) {
      topic.setSubtopics(getSubtopics(topic));
    }
    return topic;
  }

  @Override
  public Topic findOne(Identifier identifier) {
    Topic topic = super.findOne(identifier);

    if (topic != null) {
      topic.setSubtopics(getSubtopics(topic));
    }
    return topic;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public List<Subtopic> getSubtopics(UUID uuid) {
    final String stTableAlias = subtopicRepositoryImpl.getTableAlias();
    final String stTableName = subtopicRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + stTableName
                + " AS "
                + stTableAlias
                + " INNER JOIN topic_subtopics ts ON "
                + stTableAlias
                + ".uuid = ts.subtopic_uuid"
                + " WHERE ts.topic_uuid = :uuid"
                + " ORDER BY ts.sortIndex ASC");

    List<Subtopic> result =
        subtopicRepositoryImpl.retrieveList(
            subtopicRepositoryImpl.getSqlReducedFields(), innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public Topic save(Topic topic) {
    topic.setUuid(UUID.randomUUID());
    topic.setCreated(LocalDateTime.now());
    topic.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(topic)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public Topic update(Topic topic) {
    topic.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(topic)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(topic);
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    Topic result = findOne(topic.getUuid());
    return result;
  }
}
