package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
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

  public static String getSqlInsertFields() {
    return GeoLocationRepositoryImpl.getSqlInsertFields() + ", settlement_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return GeoLocationRepositoryImpl.getSqlInsertValues() + ", :humanSettlementType";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return GeoLocationRepositoryImpl.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".settlement_type "
        + mappingPrefix
        + "_humanSettlementType";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return GeoLocationRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".settlement_type "
        + mappingPrefix
        + "_humanSettlementType";
  }

  public static String getSqlUpdateFieldValues() {
    return GeoLocationRepositoryImpl.getSqlUpdateFieldValues()
        + ", settlement_type=:humanSettlementType";
  }

  @Autowired
  public HumanSettlementRepositoryImpl(Jdbi dbi) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HumanSettlement.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  public HumanSettlement save(HumanSettlement humanSettlement) {
    super.save(humanSettlement);
    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }

  @Override
  public HumanSettlement update(HumanSettlement humanSettlement) {
    super.update(humanSettlement);
    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }
}
