package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.GeoLocationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.impl.identifiable.entity.geo.GeoLocationImpl;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeoLocationRepositoryImpl extends EntityRepositoryImpl<GeoLocation>
    implements GeoLocationRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "gl";
  public static final String TABLE_ALIAS = "g";
  public static final String TABLE_NAME = "geolocations";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".coordinate_location "
        + mappingPrefix
        + "_coordinateLocation";
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".geolocation_type "
        + mappingPrefix
        + "_geoLocationType";
  }

  @Autowired
  public GeoLocationRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, GeoLocationImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public GeoLocation save(GeoLocation geoLocation) {
    if (geoLocation.getUuid() == null) {
      geoLocation.setUuid(UUID.randomUUID());
    }
    geoLocation.setCreated(LocalDateTime.now());
    geoLocation.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        geoLocation.getPreviewImage() == null ? null : geoLocation.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, previewFileResource, label, description, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type, geolocation_type,"
            + " created, last_modified,"
            + " coordinate_location"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
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
        "UPDATE "
            + tableName
            + " SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
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
    identifierRepository.deleteByIdentifiable(geoLocation);
    Set<Identifier> identifiers = geoLocation.getIdentifiers();
    saveIdentifiers(identifiers, geoLocation);

    GeoLocation result = findOne(geoLocation.getUuid());
    return result;
  }
}
