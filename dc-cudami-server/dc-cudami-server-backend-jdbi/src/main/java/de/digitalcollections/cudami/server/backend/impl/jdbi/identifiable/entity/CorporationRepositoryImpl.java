package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CorporationRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
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
public class CorporationRepositoryImpl extends EntityRepositoryImpl<Corporation>
    implements CorporationRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporationRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.text c_text, c.homepage_url c_homepageUrl,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM corporations as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
          + " FROM corporations as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  @Autowired
  public CorporationRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM corporations";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Corporation> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<CorporationImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(CorporationImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, CorporationImpl>(),
                        (map, rowView) -> {
                          CorporationImpl corporation =
                              map.computeIfAbsent(
                                  rowView.getColumn("c_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(CorporationImpl.class);
                                  });

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            corporation.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
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
  public Corporation findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE c.uuid = :uuid";

    CorporationImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CorporationImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporationImpl>(),
                            (map, rowView) -> {
                              CorporationImpl corporation =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CorporationImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                corporation.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                corporation.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public Corporation findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<CorporationImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(CorporationImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporationImpl>(),
                            (map, rowView) -> {
                              CorporationImpl corporation =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CorporationImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                corporation.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                corporation.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values().stream()
            .findFirst();
    return result.get();
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"c.created", "c.last_modified", "c.refid"};
  }

  @Override
  public Corporation save(Corporation corporation) {
    corporation.setUuid(UUID.randomUUID());
    corporation.setCreated(LocalDateTime.now());
    corporation.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        corporation.getPreviewImage() == null ? null : corporation.getPreviewImage().getUuid();

    String query =
        "INSERT INTO corporations("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, homepage_url"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :homepageUrl"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporation)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = corporation.getIdentifiers();
    saveIdentifiers(identifiers, corporation);

    Corporation result = findOne(corporation.getUuid());
    return result;
  }

  @Override
  public Corporation update(Corporation corporation) {
    corporation.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        corporation.getPreviewImage() == null ? null : corporation.getPreviewImage().getUuid();

    String query =
        "UPDATE corporations SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, homepage_url=:homepageUrl"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporation)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(corporation);
    Set<Identifier> identifiers = corporation.getIdentifiers();
    saveIdentifiers(identifiers, corporation);

    Corporation result = findOne(corporation.getUuid());
    return result;
  }
}
