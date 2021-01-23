package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
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

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
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

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Autowired
  public CorporateBodyRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        CorporateBodyImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId"};
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
    corporateBody.setUuid(UUID.randomUUID());
    corporateBody.setCreated(LocalDateTime.now());
    corporateBody.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, homepage_url"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :homepageUrl"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporateBody)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }

  @Override
  public CorporateBody update(CorporateBody corporateBody) {
    corporateBody.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, homepage_url=:homepageUrl"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(corporateBody)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(corporateBody);
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBody result = findOne(corporateBody.getUuid());
    return result;
  }
}
