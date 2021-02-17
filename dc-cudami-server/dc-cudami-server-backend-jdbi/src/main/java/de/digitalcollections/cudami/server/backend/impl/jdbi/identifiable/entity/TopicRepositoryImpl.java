package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.SubtopicRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.BreadcrumbNavigation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
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
    super(dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Topic.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
    this.subtopicRepositoryImpl = subtopicRepositoryImpl;
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Topic> children) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Topic findOne(UUID uuid, Filtering filtering) {
    Topic topic = super.findOne(uuid, filtering);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
  }

  @Override
  public Topic findOne(Identifier identifier) {
    Topic topic = super.findOne(identifier);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<Topic> getChildren(UUID nodeUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Entity> getEntities(UUID topicUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> getFileResources(UUID topicUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Topic getParent(UUID nodeUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Topic> getParents(UUID uuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<Topic> getRootNodes(PageRequest pageRequest) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Topic> getChildren(UUID uuid) {
    final String stTableAlias = getTableAlias();
    final String stTableName = getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + stTableName
                + " AS "
                + stTableAlias
                + " INNER JOIN topic_subtopics ts ON " // FIXME table name and flyway
                + stTableAlias
                + ".uuid = ts.subtopic_uuid"
                + " WHERE ts.topic_uuid = :uuid"
                + " ORDER BY ts.sortIndex ASC");

    List<Topic> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            Map.of("uuid", uuid),
            null);
    return result;
  }

  @Override
  public List<Topic> getTopicsOfEntity(UUID entityUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Topic> getTopicsOfFileResource(UUID fileResourceUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Topic save(Topic topic) {
    super.save(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public List<Entity> saveEntities(UUID topicUuid, List<Entity> entities) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> saveFileResources(UUID topicUuid, List<FileResource> fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Topic saveWithParent(Topic child, UUID parentUUID) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Topic update(Topic topic) {
    super.update(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Topic> children) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
