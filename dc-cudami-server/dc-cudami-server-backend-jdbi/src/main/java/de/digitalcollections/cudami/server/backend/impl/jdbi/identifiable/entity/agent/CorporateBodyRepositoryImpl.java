package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CorporateBodyRepositoryImpl extends EntityRepositoryImpl<CorporateBody>
    implements CorporateBodyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodyRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "cb";
  public static final String TABLE_ALIAS = "c";
  public static final String TABLE_NAME = "corporatebodies";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields() + ", homepage_url, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues() + ", :homepageUrl, :text::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text, "
        + tableAlias
        + ".homepage_url "
        + mappingPrefix
        + "_homepageUrl";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", homepage_url=:homepageUrl, text=:text::JSONB";
  }

  @Autowired
  public CorporateBodyRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        CorporateBodyImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public CorporateBody save(CorporateBody corporateBody) {
    super.save(corporateBody);
    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }

  @Override
  public CorporateBody update(CorporateBody corporateBody) {
    super.update(corporateBody);
    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }
}
