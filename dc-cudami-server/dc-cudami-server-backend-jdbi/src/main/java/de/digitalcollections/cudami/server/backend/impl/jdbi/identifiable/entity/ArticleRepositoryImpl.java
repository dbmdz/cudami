package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.entity.agent.Family;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.FamilyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl extends EntityRepositoryImpl<Article>
    implements ArticleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticleRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT a.uuid a_uuid, a.refid a_refId, a.label a_label, a.description a_description,"
          + " a.identifiable_type a_type, a.entity_type a_entityType,"
          + " a.created a_created, a.last_modified a_lastModified,"
          + " a.text a_text, a.preview_hints a_previewImageRenderingHints,"
          + " a.date_published a_datePublished, a.timevalue_published a_timeValuePublished,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM articles as a"
          + " LEFT JOIN identifiers as id on a.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on a.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT a.uuid a_uuid, a.refid a_refId, a.label a_label, a.description a_description,"
          + " a.identifiable_type a_type, a.entity_type a_entityType,"
          + " a.created a_created, a.last_modified a_lastModified,"
          + " a.preview_hints a_previewImageRenderingHints,"
          + " a.date_published a_datePublished, a.timevalue_published a_timeValuePublished,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM articles as a"
          + " LEFT JOIN fileresources_image as file on a.previewfileresource = file.uuid";

  @Autowired
  public ArticleRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM articles";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Article> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ArticleImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ArticleImpl>(),
                            (map, rowView) -> {
                              ArticleImpl article =
                                  map.computeIfAbsent(
                                      rowView.getColumn("a_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ArticleImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                article.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Article findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE a.uuid = :uuid";

    ArticleImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ArticleImpl>(),
                            (map, rowView) -> {
                              ArticleImpl article =
                                  map.computeIfAbsent(
                                      rowView.getColumn("a_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ArticleImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                article.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                article.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);

    if (result != null) {
      List<Agent> creators = getCreators(uuid);
      result.setCreators(creators);
    }
    return result;
  }

  @Override
  public Article findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ArticleImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ArticleImpl>(),
                            (map, rowView) -> {
                              ArticleImpl article =
                                  map.computeIfAbsent(
                                      rowView.getColumn("a_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ArticleImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                article.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                article.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    ArticleImpl article = result.orElse(null);

    if (article != null) {
      article.setCreators(getCreators(article.getUuid()));
    }
    return article;
  }

  @Override
  public Article save(Article article) {
    article.setUuid(UUID.randomUUID());
    article.setCreated(LocalDateTime.now());
    article.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        article.getPreviewImage() == null ? null : article.getPreviewImage().getUuid();

    String query =
        "INSERT INTO articles("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " date_published, timevalue_published"
            + " text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :datePublished, :timeValuePublished::JSONB"
            + " :text::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
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

  @Override
  public Article update(Article article) {
    article.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        article.getPreviewImage() == null ? null : article.getPreviewImage().getUuid();

    String query =
        "UPDATE articles SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " date_published=:datePublished , timevalue_published=:timeValuePublished::JSONB"
            + " text=:text::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(article)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(article);
    Set<Identifier> identifiers = article.getIdentifiers();
    saveIdentifiers(identifiers, article);

    // save creators
    List<Agent> creators = article.getCreators();
    saveCreatorsList(article, creators);

    Article result = findOne(article.getUuid());
    return result;
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
        return "a.created";
      case "lastModified":
        return "a.last_modified";
      case "refId":
        return "a.refid";
      default:
        return null;
    }
  }

  @Override
  public List<Agent> getCreators(UUID articleUuid) {
    String query =
        "SELECT e.uuid e_uuid, e.label e_label, e.refid e_refId, e.entity_type e_entityType,"
            + " e.created e_created, e.last_modified e_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM entities as e"
            + " LEFT JOIN identifiers as id on e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid"
            + " LEFT JOIN article_creators as ac on e.uuid = ac.agent_uuid"
            + " WHERE ac.article_uuid = :uuid"
            + " ORDER BY ac.sortIndex ASC";

    List<Agent> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", articleUuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Entity>(),
                        (map, rowView) -> {
                          Entity entity =
                              map.computeIfAbsent(
                                  rowView.getColumn("e_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(EntityImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            entity.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            entity.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(
                        (entity) -> {
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
}
