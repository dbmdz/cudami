package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl extends EntityRepositoryImpl<Article>
    implements ArticleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ar";
  public static final String TABLE_ALIAS = "a";
  public static final String TABLE_NAME = "articles";

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;

  public ArticleRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Article.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    this.entityRepositoryImpl = entityRepositoryImpl;
  }

  @Override
  public boolean addCreators(UUID articleUuid, List<Agent> agents) throws RepositoryException {
    if (articleUuid == null || agents == null) {
      return false;
    }
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "article_creators", "article_uuid", articleUuid);

    // save relation to article
    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO article_creators(article_uuid, agent_uuid, sortIndex) VALUES (:uuid, :agentUuid, :sortIndex) ON CONFLICT (article_uuid, agent_uuid) DO NOTHING");
          agents.forEach(
              agent -> {
                preparedBatch
                    .bind("uuid", articleUuid)
                    .bind("agentUuid", agent.getUuid())
                    .bind("sortIndex", nextSortIndex + getIndex(agents, agent))
                    .add();
              });
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public Article create() throws RepositoryException {
    return new Article();
  }

  @Override
  public Article getByIdentifier(Identifier identifier) throws RepositoryException {
    Article article = super.getByIdentifier(identifier);

    if (article != null) {
      article.setCreators(getCreators(article.getUuid()));
    }
    return article;
  }

  @Override
  public Article getByRefId(long refId) throws RepositoryException {
    Article article = super.getByRefId(refId);

    if (article != null) {
      article.setCreators(getCreators(article.getUuid()));
    }
    return article;
  }

  @Override
  public List<Article> getByUuidsAndFiltering(List<UUID> uuids, Filtering filtering)
      throws RepositoryException {
    List<Article> articles = super.getByUuidsAndFiltering(uuids, filtering);

    Optional.ofNullable(articles)
        .map(List::parallelStream)
        .ifPresent(
            stream ->
                stream.forEach(
                    article -> {
                      try {
                        article.setCreators(getCreators(article.getUuid()));
                      } catch (RepositoryException e) {
                        LOGGER.error(
                            "Cannot get creators of article with UUID %s: %s"
                                .formatted(article.getUuid(), e),
                            e);
                      }
                    }));
    return articles;
  }

  @Override
  public List<Agent> getCreators(UUID articleUuid) throws RepositoryException {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT ac.sortindex AS idx, * FROM "
                + EntityRepositoryImpl.TABLE_NAME
                + " AS e"
                + " LEFT JOIN article_creators AS ac ON e.uuid = ac.agent_uuid"
                + " WHERE ac.article_uuid = :uuid"
                + " ORDER BY ac.sortindex ASC");
    final String fieldsSql = entityRepositoryImpl.getSqlSelectReducedFields();

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", articleUuid);
    List<Entity> entityList =
        entityRepositoryImpl.retrieveList(
            fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    List<Agent> agents = null;
    if (entityList != null) {
      agents =
          entityList.stream()
              .map(
                  (entity) -> {
                    // FIXME: use new agentrepositoryimpl (see workrepositoryimpl)
                    IdentifiableObjectType type = entity.getIdentifiableObjectType();
                    switch (type) {
                      case CORPORATE_BODY:
                        CorporateBody corporateBody = new CorporateBody();
                        corporateBody.setLabel(entity.getLabel());
                        corporateBody.setRefId(entity.getRefId());
                        corporateBody.setUuid(entity.getUuid());
                        return corporateBody;
                      case FAMILY:
                        Family family = new Family();
                        family.setLabel(entity.getLabel());
                        family.setRefId(entity.getRefId());
                        family.setUuid(entity.getUuid());
                        return family;
                      case PERSON:
                        Person person = new Person();
                        person.setLabel(entity.getLabel());
                        person.setRefId(entity.getRefId());
                        person.setUuid(entity.getUuid());
                        return person;
                      default:
                        return null;
                    }
                  })
              .collect(Collectors.toList());
    }

    return agents;
  }

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", date_published, text, timevalue_published";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :datePublished, :text::JSONB, :timeValuePublished::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", date_published=:datePublished, text=:text::JSONB, timevalue_published=:timeValuePublished::JSONB";
  }

  @Override
  public boolean removeCreator(UUID articleUuid, UUID agentUuid) throws RepositoryException {
    if (articleUuid != null && agentUuid != null) {
      // delete relation to article
      final String sql =
          "DELETE FROM article_creators WHERE article_uuid=:articleUuid AND agent_uuid=:agentUuid";

      dbi.withHandle(
          h ->
              h.createUpdate(sql)
                  .bind("articleUuid", articleUuid)
                  .bind("agentUuid", agentUuid)
                  .execute());
      return true;
    }
    return false;
  }

  @Override
  public void save(Article article) throws RepositoryException, ValidationException {
    super.save(article);

    // save creators
    List<Agent> creators = article.getCreators();
    setCreatorsList(article, creators);
  }

  private void setCreatorsList(Article article, List<Agent> creators) {
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
                    "INSERT INTO article_creators(article_uuid, agent_uuid, sortIndex) VALUES(:uuid, :agentUuid, :sortIndex)");
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
  public void update(Article article) throws RepositoryException, ValidationException {
    super.update(article);

    // save creators
    List<Agent> creators = article.getCreators();
    setCreatorsList(article, creators);
  }
}
