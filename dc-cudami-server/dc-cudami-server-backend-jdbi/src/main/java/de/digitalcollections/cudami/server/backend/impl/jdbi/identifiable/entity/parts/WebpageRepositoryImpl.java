package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
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
public class WebpageRepositoryImpl<E extends Entity, C extends Comparable<C>>
    extends EntityPartRepositoryImpl<Webpage, E> implements WebpageRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.text w_text, w.publication_start w_publicationStart, w.publication_end w_publicationEnd,"
          + " w.rendering_hints w_renderingHints,"
          + " w.preview_hints w_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM webpages as w"
          + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.publication_start w_publicationStart, w.publication_end w_publicationEnd,"
          + " w.rendering_hints w_renderingHints,"
          + " w.preview_hints w_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM webpages as w"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  private static final String BASE_CHILDREN_QUERY =
      "SELECT w.uuid w_uuid, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.publication_start w_publicationStart, w.publication_end w_publicationEnd,"
          + " w.rendering_hints w_renderingHints,"
          + " w.preview_hints w_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM webpages as w INNER JOIN webpage_webpages ww ON w.uuid = ww.child_webpage_uuid"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid"
          + " WHERE ww.parent_webpage_uuid = :uuid";

  private static final String BREADCRUMB_QUERY =
      "WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
          + " AS ("
          + "        SELECT w.uuid as uuid, w.label as label, ww.parent_webpage_uuid as parent_uuid,99 as depth"
          + "        FROM webpages w, webpage_webpages ww"
          + "        WHERE uuid= :uuid and ww.child_webpage_uuid = w.uuid"
          + ""
          + "        UNION ALL"
          + "        SELECT w.uuid as uuid, w.label as label, ww.parent_webpage_uuid as parent_uuid, depth-1 as depth"
          + "        FROM webpages w,"
          + "             webpage_webpages ww,"
          + "             breadcrumb b"
          + "        WHERE b.uuid = ww.child_webpage_uuid and ww.parent_webpage_uuid = w.uuid AND ww.parent_webpage_uuid is not null"
          + "    )"
          + " SELECT * from breadcrumb"
          + " UNION"
          + " SELECT null as uuid, w.label as label, null as parent_uuid, 0 as depth"
          + " FROM websites w, website_webpages ww, breadcrumb b"
          + " WHERE ww.webpage_uuid = b.parent_uuid and w.uuid = ww.website_uuid"
          + " ORDER BY depth ASC";

  private static final String BREADCRUMB_WITHOUT_PARENT_QUERY =
      "SELECT w.uuid as uuid, w.label as label"
          + "        FROM webpages w"
          + "        WHERE uuid= :uuid";

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
        new ArrayList(
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
                                webpage.setPreviewImage(
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
            .values()
            .stream()
            .findFirst();
    Webpage webpage = result.orElse(null);
    if (webpage != null) {
      // TODO could be replaced with another join in above query...
      webpage.setChildren(getChildren(webpage));
    }
    return webpage;
  }

  @Override
  public Webpage findOne(UUID uuid, Filtering filtering) {
    String query = FIND_ONE_BASE_SQL + " WHERE w.uuid = :uuid";

    if (filtering != null) {
      // handle optional filtering params
      String filterClauses = getFilterClauses(filtering);
      if (!filterClauses.isEmpty()) {
        query += " AND " + filterClauses;
      }
    }
    String finalQuery = query;
    WebpageImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(finalQuery)
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
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query = BASE_CHILDREN_QUERY + " ORDER BY ww.sortIndex ASC";

    List<Webpage> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
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
                                webpage.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    return result;
  }

  @Override
  public PageResponse<Webpage> getChildren(UUID uuid, PageRequest pageRequest) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    StringBuilder query = new StringBuilder(BASE_CHILDREN_QUERY);

    // handle optional filtering params
    Filtering filtering = pageRequest.getFiltering();
    if (filtering != null) {
      // handle publication start criteria
      FilterCriterion<C> fc =
          (FilterCriterion<C>) filtering.getFilterCriterionFor("publicationStart");
      if (fc != null) {
        query.append(" AND ").append(getWhereClause(fc));
      }

      // handle publication end criteria
      fc = (FilterCriterion<C>) filtering.getFilterCriterionFor("publicationEnd");
      if (fc != null) {
        if (fc.getOperation() == FilterOperation.GREATER_THAN_OR_EQUAL_TO) {
          query
              .append(" AND (")
              .append(getWhereClause(fc))
              .append(" OR ")
              .append(getColumnName("publicationEnd"))
              .append(" IS NULL")
              .append(")");
        } else {
          query.append(" AND ").append(getWhereClause(fc));
        }
      }
    }
    if (pageRequest.getSorting() == null) {
      query.append(" ORDER BY ww.sortIndex ASC");
    }
    addPageRequestParams(pageRequest, query);
    List<Webpage> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("uuid", uuid)
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
                                webpage.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    String sql =
        "SELECT count(*) FROM webpages as w"
            + " INNER JOIN webpage_webpages ww ON w.uuid = ww.child_webpage_uuid"
            + " WHERE ww.parent_webpage_uuid = :uuid";
    long total =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Long.class).findOne().get());
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
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
            .values()
            .stream()
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
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type,"
            + " created, last_modified,"
            + " text, publication_start, publication_end,"
            + " rendering_hints"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type,"
            + " :created, :lastModified,"
            + " :text::JSONB, :publicationStart, :publicationEnd,"
            + " :renderingHints::JSONB"
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
        retrieveNextSortIndexForParentChildren(
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
        retrieveNextSortIndexForParentChildren(
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
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd,"
            + " rendering_hints=:renderingHints::JSONB"
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

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "publicationEnd", "publicationStart"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "w.created";
      case "lastModified":
        return "w.last_modified";
      case "publicationEnd":
        return "w.publication_end";
      case "publicationStart":
        return "w.publication_start";
      default:
        return null;
    }
  }

  @Override
  public Website getWebsite(UUID rootWebpageUuid) {
    String query =
        "SELECT uuid, refid, label"
            + " FROM websites"
            + " INNER JOIN website_webpages ww ON uuid = ww.website_uuid"
            + " WHERE ww.webpage_uuid = :uuid";

    WebsiteImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", rootWebpageUuid)
                    .mapToBean(WebsiteImpl.class)
                    .one());
    return result;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {

    List<NodeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(BREADCRUMB_QUERY)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                    .mapTo(NodeImpl.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level webpage, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(BREADCRUMB_WITHOUT_PARENT_QUERY)
                      .bind("uuid", uuid)
                      .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                      .mapTo(NodeImpl.class)
                      .list());
    }

    List<Node> nodes = result.stream().map(s -> (Node) s).collect(Collectors.toList());

    return new BreadcrumbNavigationImpl(nodes);
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Webpage> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query =
        "UPDATE webpage_webpages"
            + " SET sortindex = :idx"
            + " WHERE child_webpage_uuid = :childWebpageUuid AND parent_webpage_uuid = :parentWebpageUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Webpage webpage : children) {
            batch
                .bind("idx", idx++)
                .bind("childWebpageUuid", webpage.getUuid())
                .bind("parentWebpageUuid", parentUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
