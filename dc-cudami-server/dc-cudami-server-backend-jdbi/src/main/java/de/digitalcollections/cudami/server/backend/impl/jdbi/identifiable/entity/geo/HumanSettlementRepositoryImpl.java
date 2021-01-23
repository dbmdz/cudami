package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.impl.identifiable.entity.geo.HumanSettlementImpl;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HumanSettlementRepositoryImpl extends EntityRepositoryImpl<HumanSettlement>
    implements HumanSettlementRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hs";
  public static final String TABLE_ALIAS = "h";
  public static final String TABLE_NAME = "humansettlements";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".coordinate_location "
        + mappingPrefix
        + "_coordinateLocation";
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return GeoLocationRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".settlement_type "
        + mappingPrefix
        + "_humanSettlementType";
  }

  @Autowired
  public HumanSettlementRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HumanSettlementImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public HumanSettlement save(HumanSettlement humanSettlement) {
    if (humanSettlement.getUuid() == null) {
      humanSettlement.setUuid(UUID.randomUUID());
    }
    humanSettlement.setCreated(LocalDateTime.now());
    humanSettlement.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        humanSettlement.getPreviewImage() == null
            ? null
            : humanSettlement.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, previewFileResource, label, description, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type, geolocation_type,"
            + " created, last_modified,"
            + " coordinate_location, settlement_type"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType, :geoLocationType,"
            + " :created, :lastModified,"
            + " :coordinateLocation::JSONB, :humanSettlementType"
            + ")";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(humanSettlement)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = humanSettlement.getIdentifiers();
    saveIdentifiers(identifiers, humanSettlement);

    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }

  @Override
  public HumanSettlement update(HumanSettlement humanSettlement) {
    // uuid and created stay unchanged, update last modified
    humanSettlement.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        humanSettlement.getPreviewImage() == null
            ? null
            : humanSettlement.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " geolocation_type=:geoLocationType,"
            + " last_modified=:lastModified,"
            + " coordinate_location=:coordinateLocation::JSONB, settlement_type=:humanSettlementType"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(humanSettlement)
                .execute());

    // save identifiers
    identifierRepository.deleteByIdentifiable(humanSettlement);
    Set<Identifier> identifiers = humanSettlement.getIdentifiers();
    saveIdentifiers(identifiers, humanSettlement);

    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }
}
