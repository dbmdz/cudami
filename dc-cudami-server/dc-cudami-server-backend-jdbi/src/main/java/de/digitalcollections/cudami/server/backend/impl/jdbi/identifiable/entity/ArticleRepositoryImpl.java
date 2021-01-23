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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
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

  private final EntityRepositoryImpl<EntityImpl> entityRepositoryImpl;

  @Autowired
  public ArticleRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<EntityImpl> entityRepositoryImpl) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, ArticleImpl.class);
    this.entityRepositoryImpl = entityRepositoryImpl;
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
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
            + entityRepositoryImpl.getSqlReducedFields()
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
    article.setUuid(UUID.randomUUID());
    article.setCreated(LocalDateTime.now());
    article.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        article.getPreviewImage() == null ? null : article.getPreviewImage().getUuid();

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " date_published, timevalue_published,"
            + " text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :datePublished, :timeValuePublished::JSONB,"
            + " :text::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(article)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = article.getIdentifiers();
    saveIdentifiers(identifiers, article);

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
    article.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        article.getPreviewImage() == null ? null : article.getPreviewImage().getUuid();

    final String sql =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified,"
            + " date_published=:datePublished, timevalue_published=:timeValuePublished::JSONB,"
            + " text=:text::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(article)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(article);
    Set<Identifier> identifiers = article.getIdentifiers();
    saveIdentifiers(identifiers, article);

    // save creators
    List<Agent> creators = article.getCreators();
    saveCreatorsList(article, creators);

    Article result = findOne(article.getUuid());
    return result;
  }
}
