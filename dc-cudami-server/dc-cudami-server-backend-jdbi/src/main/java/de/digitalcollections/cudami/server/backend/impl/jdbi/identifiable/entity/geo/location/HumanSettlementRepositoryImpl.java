package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HumanSettlementRepositoryImpl extends GeoLocationRepositoryImpl<HumanSettlement>
    implements HumanSettlementRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hs";
  public static final String TABLE_ALIAS = "h";
  public static final String TABLE_NAME = "humansettlements";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", settlement_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :humanSettlementType";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields();
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".settlement_type "
        + mappingPrefix
        + "_humanSettlementType";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", settlement_type=:humanSettlementType";
  }

  @Autowired
  public HumanSettlementRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HumanSettlement.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }
}
