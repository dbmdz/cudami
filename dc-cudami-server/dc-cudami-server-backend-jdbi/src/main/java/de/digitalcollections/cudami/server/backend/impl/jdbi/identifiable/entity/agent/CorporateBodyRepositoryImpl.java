package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CorporateBodyRepositoryImpl extends EntityRepositoryImpl<CorporateBodyImpl>
    implements CorporateBodyRepository<CorporateBodyImpl> {

  public static final String SQL_REDUCED_CORPORATEBODY_FIELDS_CB =
      " cb.uuid cb_uuid, cb.label cb_label, cb.refid cb_refId,"
          + " cb.created cb_created, cb.last_modified cb_lastModified,"
          + " cb.homepage_url cb_homepageUrl";

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.text c_text, c.homepage_url c_homepageUrl, c.preview_hints c_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM corporatebodies as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.preview_hints c_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM corporatebodies as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  @Autowired
  public CorporateBodyRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM corporatebodies";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<CorporateBody> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<CorporateBodyImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(CorporateBodyImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporateBodyImpl>(),
                            (map, rowView) -> {
                              CorporateBodyImpl corporateBody =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CorporateBodyImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                corporateBody.setPreviewImage(
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
  public CorporateBody findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE c.uuid = :uuid";

    CorporateBodyImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CorporateBodyImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporateBodyImpl>(),
                            (map, rowView) -> {
                              CorporateBodyImpl corporateBody =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CorporateBodyImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                corporateBody.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                corporateBody.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public CorporateBody findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<CorporateBodyImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(CorporateBodyImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporateBodyImpl>(),
                            (map, rowView) -> {
                              CorporateBodyImpl corporateBody =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CorporateBodyImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                corporateBody.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                corporateBody.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
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
        return "c.created";
      case "lastModified":
        return "c.last_modified";
      case "refId":
        return "c.refid";
      default:
        return null;
    }
  }

  @Override
  public CorporateBody save(CorporateBody corporateBody) {
    corporateBody.setUuid(UUID.randomUUID());
    corporateBody.setCreated(LocalDateTime.now());
    corporateBody.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query =
        "INSERT INTO corporatebodies("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, homepage_url"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :homepageUrl"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporateBody)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }

  @Override
  public CorporateBody update(CorporateBody corporateBody) {
    corporateBody.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query =
        "UPDATE corporatebodies SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, homepage_url=:homepageUrl"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporateBody)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(corporateBody);
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }

  public static BiFunction<
          LinkedHashMap<UUID, CorporateBody>, RowView, LinkedHashMap<UUID, CorporateBody>>
      mapRowToCorporateBody() {
    return mapRowToCorporateBody(false);
  }

  public static BiFunction<
          LinkedHashMap<UUID, CorporateBody>, RowView, LinkedHashMap<UUID, CorporateBody>>
      mapRowToCorporateBody(boolean withIdentifiers) {
    return (map, rowView) -> {
      CorporateBody corporateBody =
          map.computeIfAbsent(
              rowView.getColumn("cb_uuid", UUID.class),
              fn -> {
                return rowView.getRow(CorporateBodyImpl.class);
              });

      if (rowView.getColumn("pi_uuid", UUID.class) != null) {
        corporateBody.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
        corporateBody.addIdentifier(dbIdentifier);
      }
      return map;
    };
  }
}
