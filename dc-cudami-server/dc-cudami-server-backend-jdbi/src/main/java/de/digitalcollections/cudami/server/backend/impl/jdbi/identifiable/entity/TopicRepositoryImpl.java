package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.SubtopicRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<TopicImpl> implements TopicRepository<TopicImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_TO
          = " t.uuid to_uuid, t.refid to_refId, t.label to_label, t.description to_description,"
          + " t.identifiable_type to_type, t.entity_type to_entityType,"
          + " t.created to_created, t.last_modified to_lastModified,"
          + " t.preview_hints to_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_TO = SQL_REDUCED_FIELDS_TO;

  public static final String TABLE_NAME = "topics";

  private final SubtopicRepositoryImpl subtopicRepositoryImpl;

  @Autowired
  public TopicRepositoryImpl(Jdbi dbi,
          IdentifierRepository identifierRepository,
          SubtopicRepositoryImpl subtopicRepositoryImpl) {
    super(
            dbi,
            identifierRepository,
            TABLE_NAME,
            "t",
            "to",
            TopicImpl.class,
            SQL_REDUCED_FIELDS_TO,
            SQL_FULL_FIELDS_TO);
    this.subtopicRepositoryImpl = subtopicRepositoryImpl;
  }

  @Override
  public TopicImpl findOne(UUID uuid, Filtering filtering) {
    TopicImpl topic = super.findOne(uuid, filtering);

    if (topic != null) {
      topic.setSubtopics(
              Stream.ofNullable(getSubtopics(topic))
                      .map(Subtopic.class::cast)
                      .collect(Collectors.toList()));
    }
    return topic;
  }

  @Override
  public TopicImpl findOne(Identifier identifier) {
    TopicImpl topic = super.findOne(identifier);

    if (topic != null) {
      // TODO could be replaced with another join in above query...
      topic.setSubtopics(
              Stream.ofNullable(getSubtopics(topic))
                      .map(Subtopic.class::cast)
                      .collect(Collectors.toList()));
    }
    return topic;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "lastModified", "refId"};
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
  public List<Subtopic> getSubtopics(TopicImpl topic) {
    UUID uuid = topic.getUuid();
    return getSubtopics(uuid);
  }

  @Override
  public List<Subtopic> getSubtopics(UUID uuid) {
    final String stTableAlias = subtopicRepositoryImpl.getTableAlias();
    final String stTableName = subtopicRepositoryImpl.getTableName();

    StringBuilder innerQuery
            = new StringBuilder("SELECT * FROM "
                    + stTableName
                    + " AS "
                    + stTableAlias
                    + " INNER JOIN topic_subtopics ts ON "
                    + stTableAlias
                    + ".uuid = ts.subtopic_uuid"
                    + " WHERE ts.topic_uuid = :uuid"
                    + " ORDER BY ts.sortIndex ASC");

    List<SubtopicImpl> result = subtopicRepositoryImpl.retrieveList(SubtopicRepositoryImpl.SQL_REDUCED_FIELDS_ST, innerQuery, Map.of("uuid", uuid));
    return result.stream().map(Subtopic.class::cast)
            .collect(Collectors.toList());
  }

  @Override
  public TopicImpl save(TopicImpl topic) {
    topic.setUuid(UUID.randomUUID());
    topic.setCreated(LocalDateTime.now());
    topic.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid
            = topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    final String sql
            = "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
            h
            -> h.createUpdate(sql)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(topic)
                    .execute());

    // save identifiers
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    TopicImpl result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public TopicImpl update(TopicImpl topic) {
    topic.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid
            = topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    String query
            = "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(topic)
                    .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(topic);
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    TopicImpl result = findOne(topic.getUuid());
    return result;
  }
}
