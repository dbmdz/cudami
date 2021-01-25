package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.entity.agent.Family;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.FamilyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl extends EntityRepositoryImpl<Article>
    implements ArticleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ar";
  public static final String TABLE_ALIAS = "a";
  public static final String TABLE_NAME = "articles";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields()
        + ", date_published, text, timevalue_published";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues()
        + ", :datePublished, :text::JSONB, :timeValuePublished::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".date_published "
        + mappingPrefix
        + "_datePublished, "
        + tableAlias
        + ".timevalue_published "
        + mappingPrefix
        + "_timeValuePublished";
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", date_published=:datePublished, text=:text::JSONB, timevalue_published=:timeValuePublished::JSONB";
  }

  private final EntityRepositoryImpl<EntityImpl> entityRepositoryImpl;

  @Autowired
  public ArticleRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<EntityImpl> entityRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        ArticleImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
    this.entityRepositoryImpl = entityRepositoryImpl;
  }

  @Override
  public Article findOne(UUID uuid, Filtering filtering) {
    Article article = super.findOne(uuid, filtering);

    if (article != null) {
      List<Agent> creators = getCreators(uuid);
      article.setCreators(creators);
    }
    return article;
  }

  @Override
  public Article findOne(Identifier identifier) {
    Article article = super.findOne(identifier);

    if (article != null) {
      article.setCreators(getCreators(article.getUuid()));
    }
    return article;
  }

  @Override
  public Article findOneByRefId(long refId) {
    Article article = super.findOneByRefId(refId);

    if (article != null) {
      article.setCreators(getCreators(article.getUuid()));
    }
    return article;
  }

  @Override
  public List<Agent> getCreators(UUID articleUuid) {
    String innerQuery =
        "SELECT * FROM "
            + EntityRepositoryImpl.TABLE_NAME
            + " AS e"
            + " LEFT JOIN article_creators AS ac ON e.uuid = ac.agent_uuid"
            + " WHERE ac.article_uuid = :uuid"
            + " ORDER BY ac.sortindex ASC";

    final String sql =
        "SELECT"
            + entityRepositoryImpl.getSqlSelectReducedFields()
            + ","
            + SQL_FULL_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    List<Agent> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("uuid", articleUuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        entityRepositoryImpl.basicReduceRowsBiFunction)
                    .values()
                    .stream()
                    .map(
                        (entity) -> {
                          // FIXME: use new agentrepositoryimpl (see workrepositoryimpl)
                          EntityType entityType = entity.getEntityType();
                          switch (entityType) {
                            case CORPORATE_BODY:
                              CorporateBody corporateBody = new CorporateBodyImpl();
                              corporateBody.setLabel(entity.getLabel());
                              corporateBody.setRefId(entity.getRefId());
                              corporateBody.setUuid(entity.getUuid());
                              return corporateBody;
                            case FAMILY:
                              Family family = new FamilyImpl();
                              family.setLabel(entity.getLabel());
                              family.setRefId(entity.getRefId());
                              family.setUuid(entity.getUuid());
                              return family;
                            case PERSON:
                              Person person = new PersonImpl();
                              person.setLabel(entity.getLabel());
                              person.setRefId(entity.getRefId());
                              person.setUuid(entity.getUuid());
                              return person;
                            default:
                              return null;
                          }
                        })
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    return entityRepositoryImpl.getRelatedFileResources(entityUuid);
  }

  @Override
  public Article save(Article article) {
    super.save(article);

    // save creators
    List<Agent> creators = article.getCreators();
    saveCreatorsList(article, creators);

    Article result = findOne(article.getUuid());
    return result;
  }

  private void saveCreatorsList(Article article, List<Agent> creators) {
    UUID articleUuid = article.getUuid();

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM article_creators WHERE article_uuid = :uuid")
                .bind("uuid", articleUuid)
                .execute());

    if (creators != null) {
      // second: save relations
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO article_creators(work_uuid, agent_uuid, sortIndex) VALUES(:uuid, :agentUuid, :sortIndex)");
            for (Agent agent : creators) {
              preparedBatch
                  .bind("uuid", articleUuid)
                  .bind("agentUuid", agent.getUuid())
                  .bind("sortIndex", getIndex(creators, agent))
                  .add();
            }
            preparedBatch.execute();
          });
    }
  }

  @Override
  public Article update(Article article) {
    super.update(article);

    // save creators
    List<Agent> creators = article.getCreators();
    saveCreatorsList(article, creators);

    Article result = findOne(article.getUuid());
    return result;
  }
}
