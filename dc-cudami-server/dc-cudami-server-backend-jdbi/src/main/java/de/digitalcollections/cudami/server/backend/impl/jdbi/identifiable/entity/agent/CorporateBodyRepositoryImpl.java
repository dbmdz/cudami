package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CorporateBodyRepositoryImpl extends AgentRepositoryImpl<CorporateBody>
    implements CorporateBodyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodyRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "cb";
  public static final String TABLE_ALIAS = "c";
  public static final String TABLE_NAME = "corporatebodies";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", homepage_url, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :homepageUrl, :text::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".homepage_url "
        + mappingPrefix
        + "_homepageUrl";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", homepage_url=:homepageUrl, text=:text::JSONB";
  }

  @Autowired
  public CorporateBodyRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        CorporateBody.class,
        null,
        null,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "homepageUrl":
        return tableAlias + ".homepage_url";
      default:
        return null;
    }
  }

  @Override
  public CorporateBody save(CorporateBody corporateBody) {
    super.save(corporateBody);
    CorporateBody result = getByUuid(corporateBody.getUuid());
    return result;
  }

  @Override
  public CorporateBody update(CorporateBody corporateBody) {
    super.update(corporateBody);
    CorporateBody result = getByUuid(corporateBody.getUuid());
    return result;
  }
}
