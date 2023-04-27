package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HumanSettlementRepositoryImpl extends GeoLocationRepositoryImpl<HumanSettlement>
    implements HumanSettlementRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hs";
  public static final String TABLE_ALIAS = "h";
  public static final String TABLE_NAME = "humansettlements";

  public HumanSettlementRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HumanSettlement.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public HumanSettlement create() throws RepositoryException {
    return new HumanSettlement();
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", settlement_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :humanSettlementType";
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
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", settlement_type=:humanSettlementType";
  }
}
