package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<Webpage, E>
    implements WebpageRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.text w_text,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM webpages as w"
          + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
          + " FROM webpages as w"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM webpages";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<WebpageImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(WebpageImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, WebpageImpl>(),
                        (map, rowView) -> {
                          WebpageImpl webpage =
                              map.computeIfAbsent(
                                  rowView.getColumn("w_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(WebpageImpl.class);
                                  });

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            webpage.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .collect(Collectors.toList()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Webpage findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE w.uuid = :uuid";

    WebpageImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(WebpageImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WebpageImpl>(),
                            (map, rowView) -> {
                              WebpageImpl webpage =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(WebpageImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                webpage.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                webpage.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);

    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setChildren(getChildren(result));
    }
    return result;
  }

  @Override
  public Webpage findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<WebpageImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(WebpageImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WebpageImpl>(),
                            (map, rowView) -> {
                              WebpageImpl webpage =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(WebpageImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                webpage.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                webpage.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values().stream()
            .findFirst();
    Webpage webpage = result.orElse(null);
    if (webpage != null) {
      // TODO could be replaced with another join in above query...
      webpage.setChildren(getChildren(webpage));
    }
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"w.created", "w.last_modified"};
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT w.uuid w_uuid, w.label w_label, w.description w_description,"
            + " w.identifiable_type w_type,"
            + " w.created w_created, w.last_modified w_lastModified,"
            + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
            + " FROM webpages as w INNER JOIN webpage_webpages ww ON w.uuid = ww.child_webpage_uuid"
            + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid"
            + " WHERE ww.parent_webpage_uuid = :uuid"
            + " ORDER BY ww.sortIndex ASC";

    List<Webpage> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(WebpageImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, WebpageImpl>(),
                        (map, rowView) -> {
                          WebpageImpl webpage =
                              map.computeIfAbsent(
                                  rowView.getColumn("w_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(WebpageImpl.class);
                                  });

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            webpage.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Webpage getParent(UUID uuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN webpage_webpages ww ON w.uuid = ww.parent_webpage_uuid"
            + " WHERE ww.child_webpage_uuid = :uuid";

    Optional<WebpageImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(WebpageImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WebpageImpl>(),
                            (map, rowView) -> {
                              WebpageImpl parentWebpage =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(WebpageImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                parentWebpage.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              return map;
                            }))
            .values().stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public Webpage save(Webpage webpage) {
    webpage.setUuid(UUID.randomUUID());
    webpage.setCreated(LocalDateTime.now());
    webpage.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query =
        "INSERT INTO webpages("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type,"
            + " created, last_modified,"
            + " text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type,"
            + " :created, :lastModified,"
            + " :text::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(webpage)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    Webpage result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "webpage_webpages", "parent_webpage_uuid", parentWebpageUuid);

    String query =
        "INSERT INTO webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
            + " VALUES (:parent_webpage_uuid, :uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_webpage_uuid", parentWebpageUuid)
                .bind("sortIndex", sortIndex)
                .bindBean(savedWebpage)
                .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "website_webpages", "website_uuid", parentWebsiteUuid);
    String query =
        "INSERT INTO website_webpages(website_uuid, webpage_uuid, sortIndex)"
            + " VALUES (:parent_website_uuid, :uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_website_uuid", parentWebsiteUuid)
                .bind("sortIndex", sortIndex)
                .bindBean(savedWebpage)
                .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage update(Webpage webpage) {
    webpage.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query =
        "UPDATE webpages SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(webpage)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(webpage);
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    Webpage result = findOne(webpage.getUuid());
    return result;
  }
}
