package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
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
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type, w.entity_type w_entityType,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.url w_url, w.registration_date w_registrationDate,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM websites as w"
          + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type, w.entity_type w_entityType,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.url w_url, w.registration_date w_registrationDate,"
          + " file.uri f_uri, file.filename f_filename"
          + " FROM websites as w"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  @Autowired
  public WebsiteRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM websites";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Website> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<WebsiteImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(WebsiteImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, WebsiteImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, WebsiteImpl.class, "w_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Website findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE w.uuid = :uuid";

    Optional<WebsiteImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(WebsiteImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, WebsiteImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, WebsiteImpl.class, "w_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    Website website = resultOpt.get();
    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  public Website findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<WebsiteImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(WebsiteImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, WebsiteImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, WebsiteImpl.class, "w_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    Website website = resultOpt.get();
    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"a.created", "a.last_modified", "a.refid", "a.url"};
  }

  @Override
  public Website save(Website website) {
    website.setUuid(UUID.randomUUID());
    website.setCreated(LocalDateTime.now());
    website.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        website.getPreviewImage() == null ? null : website.getPreviewImage().getUuid();

    String query =
        "INSERT INTO websites("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " url, registration_date"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :url, :registrationDate"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(website)
                .execute());

    // save identifiers
    List<Identifier> identifiers = website.getIdentifiers();
    saveIdentifiers(identifiers, website);

    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public Website update(Website website) {
    website.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        website.getPreviewImage() == null ? null : website.getPreviewImage().getUuid();

    String query =
        "UPDATE websites SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
            + " url=:url, registration_date=:registrationDate"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(website)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(website);
    List<Identifier> identifiers = website.getIdentifiers();
    saveIdentifiers(identifiers, website);

    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public List<Webpage> getRootPages(Website website) {
    UUID uuid = website.getUuid();
    return getRootPages(uuid);
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql =
        "SELECT "
            + "uuid, created, description, label, last_modified"
            + " FROM webpages INNER JOIN website_webpages ww ON uuid = ww.webpage_uuid"
            + " WHERE ww.website_uuid = :uuid"
            + " ORDER BY ww.sortIndex ASC";

    List<WebpageImpl> list =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapToBean(WebpageImpl.class).list());
    return list.stream().map(WebpageImpl.class::cast).collect(Collectors.toList());
  }
}
