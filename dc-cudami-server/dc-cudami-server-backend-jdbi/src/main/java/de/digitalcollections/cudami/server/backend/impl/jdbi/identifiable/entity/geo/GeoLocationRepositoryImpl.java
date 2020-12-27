package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.GeoLocationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.geo.GeoLocationImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeoLocationRepositoryImpl extends IdentifiableRepositoryImpl<GeoLocation>
    implements GeoLocationRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationRepositoryImpl.class);

  @Autowired
  public GeoLocationRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM geolocations";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<GeoLocation> find(PageRequest pageRequest) {
    // TODO make dependend from language the user has chosen...
    String language = "de";

    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT g.uuid g_uuid, g.refid g_refId, g.label g_label, g.description g_description,"
                //      + " g.identifiable_type g_type, g.entity_type g_entityType,
                // g.geolocation_type g_geoLocationType,"
                //      + " g.created g_created, g.last_modified g_last_modified,"
                //      + " g.coordinate_location g_coordinateLocation,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uri f_uri, file.filename f_filename"
                + " FROM geolocations as g"
                //      + " LEFT JOIN identifiers as id on g.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on g.previewfileresource = file.uuid"
                + " ORDER BY g.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<GeoLocation> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .registerRowMapper(BeanMapper.factory(GeoLocationImpl.class, "g"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, GeoLocation>(),
                            (map, rowView) -> {
                              GeoLocation geoLocation =
                                  map.computeIfAbsent(
                                      rowView.getColumn("g_uuid", UUID.class),
                                      uuid -> rowView.getRow(GeoLocationImpl.class));
                              if (rowView.getColumn("f_uri", String.class) != null) {
                                geoLocation.setPreviewImage(
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
  public PageResponse<GeoLocation> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT g.uuid g_uuid, g.refid g_refId, g.label g_label, g.description g_description,"
                //      + " g.identifiable_type g_type, g.entity_type g_entityType,
                // g.geolocation_type g_geoLocationType,"
                //      + " g.created g_created, g.last_modified g_last_modified,"
                //      + " g.coordinate_location g_coordinateLocation,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uri f_uri, file.filename f_filename"
                + " FROM geolocations as g"
                //      + " LEFT JOIN identifiers as id on g.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on g.previewfileresource = file.uuid"
                + " WHERE g.label ->> :language IS NOT null AND g.label ->> :language ILIKE :initial || '%'"
                + " ORDER BY g.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<GeoLocation> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .bind("initial", initial)
                        .registerRowMapper(BeanMapper.factory(GeoLocationImpl.class, "g"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, GeoLocation>(),
                            (map, rowView) -> {
                              GeoLocation geoLocation =
                                  map.computeIfAbsent(
                                      rowView.getColumn("g_uuid", UUID.class),
                                      uuid -> rowView.getRow(GeoLocationImpl.class));
                              if (rowView.getColumn("f_uri", String.class) != null) {
                                geoLocation.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM geolocations as g"
            + " WHERE g.label ->> :language IS NOT null AND g.label ->> :language ILIKE :initial || '%'";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("language", language)
                    .bind("initial", initial)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public GeoLocation findOne(UUID uuid) {
    String query =
        "SELECT g.uuid g_uuid, g.refid g_refId, g.label g_label, g.description g_description,"
            + " g.identifiable_type g_type, g.entity_type g_entityType, g.geolocation_type g_geoLocationType,"
            + " g.created g_created, g.last_modified g_last_modified,"
            + " g.coordinate_location g_coordinateLocation,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM geolocations as g"
            + " LEFT JOIN identifiers as id on g.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on g.previewfileresource = file.uuid"
            + " WHERE g.uuid = :uuid";
    Optional<GeoLocation> resultOpt =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(GeoLocationImpl.class, "g"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, GeoLocation>(),
                        (map, rowView) -> {
                          GeoLocation geoLocation =
                              map.computeIfAbsent(
                                  rowView.getColumn("g_uuid", UUID.class),
                                  id -> rowView.getRow(GeoLocationImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            geoLocation.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            geoLocation.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public GeoLocation findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query =
        "SELECT g.uuid g_uuid, g.refid g_refId, g.label g_label, g.description g_description,"
            + " g.identifiable_type g_type, g.entity_type g_entityType, g.geolocation_type g_geoLocationType,"
            + " g.created g_created, g.last_modified g_last_modified,"
            + " g.coordinate_location g_coordinateLocation,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM geolocations as g"
            + " LEFT JOIN identifiers as id on g.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on g.previewfileresource = file.uuid"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<GeoLocation> resultOpt =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("id", identifierId)
                    .bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(GeoLocationImpl.class, "g"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, GeoLocation>(),
                        (map, rowView) -> {
                          GeoLocation geoLocation =
                              map.computeIfAbsent(
                                  rowView.getColumn("g_uuid", UUID.class),
                                  id -> rowView.getRow(GeoLocationImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            geoLocation.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            geoLocation.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public GeoLocation findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public GeoLocation save(GeoLocation geoLocation) {
    if (geoLocation.getUuid() == null) {
      geoLocation.setUuid(UUID.randomUUID());
    }
    geoLocation.setCreated(LocalDateTime.now());
    geoLocation.setLastModified(LocalDateTime.now());

    final UUID previewImageUuid =
        geoLocation.getPreviewImage() == null ? null : geoLocation.getPreviewImage().getUuid();

    String query =
        "INSERT INTO geolocations("
            + "uuid, previewFileResource, label, description,"
            + " identifiable_type, entity_type, geolocation_type,"
            + " created, last_modified,"
            + " coordinate_location"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB,"
            + " :type, :entityType, :geoLocationType,"
            + " :created, :lastModified,"
            + " :coordinateLocation::JSONB"
            + ")";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(geoLocation)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = geoLocation.getIdentifiers();
    saveIdentifiers(identifiers, geoLocation);

    GeoLocation result = findOne(geoLocation.getUuid());
    return result;
  }

  @Override
  public GeoLocation update(GeoLocation geoLocation) {
    // uuid and created stay unchanged, update last modified
    geoLocation.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        geoLocation.getPreviewImage() == null ? null : geoLocation.getPreviewImage().getUuid();

    String query =
        "UPDATE geolocations SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB,"
            + " last_modified=:lastModified,"
            + " coordinate_location=:coordinateLocation::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(geoLocation)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = geoLocation.getIdentifiers();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM identifiers WHERE identifiable = :uuid")
                .bind("uuid", geoLocation.getUuid())
                .execute());
    saveIdentifiers(identifiers, geoLocation);

    GeoLocation result = findOne(geoLocation.getUuid());
    return result;
  }
}
