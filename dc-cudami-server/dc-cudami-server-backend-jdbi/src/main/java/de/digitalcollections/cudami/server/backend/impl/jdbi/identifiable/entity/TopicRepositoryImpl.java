package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.SubtopicRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import java.util.List;
import java.util.Map;
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

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields();
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues();
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues();
  }

  private final SubtopicRepositoryImpl subtopicRepositoryImpl;

  @Autowired
  public TopicRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      SubtopicRepositoryImpl subtopicRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        TopicImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
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
            subtopicRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            Map.of("uuid", uuid),
            null);
    return result;
  }

  @Override
  public Topic save(Topic topic) {
    super.save(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public Topic update(Topic topic) {
    super.update(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }
}
