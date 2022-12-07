package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.GivenNameRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.identifiable.agent.GivenName;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GivenNameRepositoryImpl extends IdentifiableRepositoryImpl<GivenName>
    implements GivenNameRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(GivenNameRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "gn";
  public static final String TABLE_ALIAS = "g";
  public static final String TABLE_NAME = "givennames";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", gender";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :gender";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".gender "
        + mappingPrefix
        + "_gender";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", gender=:gender";
  }

  @Autowired
  public GivenNameRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        GivenName.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }
}
