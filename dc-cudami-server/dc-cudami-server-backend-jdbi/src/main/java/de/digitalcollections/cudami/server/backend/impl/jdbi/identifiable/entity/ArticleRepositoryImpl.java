package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ArticleRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.ArticleImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
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
          + " a.text a_text,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM articles as a"
          + " LEFT JOIN identifiers as id on a.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on a.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT a.uuid a_uuid, a.refid a_refId, a.label a_label, a.description a_description,"
          + " a.identifiable_type a_type, a.entity_type a_entityType,"
          + " a.created a_created, a.last_modified a_lastModified,"
          + " file.uri f_uri, file.filename f_filename"
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
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ArticleImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, ArticleImpl.class, "a_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Article findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE a.uuid = :uuid";

    Optional<ArticleImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ArticleImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ArticleImpl.class, "a_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public Article findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ArticleImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(ArticleImpl.class, "a"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ArticleImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ArticleImpl.class, "a_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"a.created", "a.last_modified", "a.refid"};
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
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(article)
                .execute());

    // save identifiers
    List<Identifier> identifiers = article.getIdentifiers();
    saveIdentifiers(identifiers, article);

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
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
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
    List<Identifier> identifiers = article.getIdentifiers();
    saveIdentifiers(identifiers, article);

    Article result = findOne(article.getUuid());
    return result;
  }
}
