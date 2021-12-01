package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.GeoLocationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
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

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields() + ", coordinate_location, geolocation_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues()
        + ", :coordinateLocation::JSONB, :geoLocationType";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".coordinate_location "
        + mappingPrefix
        + "_coordinateLocation";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".geolocation_type "
        + mappingPrefix
        + "_geoLocationType";
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", coordinate_location=:coordinateLocation::JSONB";
  }

  @Autowired
  public GeoLocationRepositoryImpl(Jdbi dbi) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        GeoLocation.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  public GeoLocation save(GeoLocation geoLocation) {
    super.save(geoLocation);
    GeoLocation result = findOne(geoLocation.getUuid());
    return result;
  }

  @Override
  public GeoLocation update(GeoLocation geoLocation) {
    super.update(geoLocation);
    GeoLocation result = findOne(geoLocation.getUuid());
    return result;
  }
}
