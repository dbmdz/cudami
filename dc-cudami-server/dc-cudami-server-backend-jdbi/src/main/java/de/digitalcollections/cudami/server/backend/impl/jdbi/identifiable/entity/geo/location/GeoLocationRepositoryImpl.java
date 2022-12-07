package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.GeoLocationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("geoLocationRepository")
public class GeoLocationRepositoryImpl<G extends GeoLocation> extends EntityRepositoryImpl<G>
    implements GeoLocationRepository<G> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "gl";
  public static final String TABLE_ALIAS = "g";
  public static final String TABLE_NAME = "geolocations";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", coordinate_location, geolocation_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :coordinateLocation::JSONB, :geoLocationType";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".coordinate_location "
        + mappingPrefix
        + "_coordinateLocation";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".geolocation_type "
        + mappingPrefix
        + "_geoLocationType";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", coordinate_location=:coordinateLocation::JSONB";
  }

  @Autowired
  public GeoLocationRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        GeoLocation.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  public GeoLocationRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends GeoLocation> entityImplClass,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, entityImplClass, offsetForAlternativePaging);
  }
}
