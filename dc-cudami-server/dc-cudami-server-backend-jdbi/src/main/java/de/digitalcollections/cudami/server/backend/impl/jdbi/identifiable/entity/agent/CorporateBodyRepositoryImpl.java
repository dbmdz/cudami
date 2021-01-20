package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
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
public class CorporateBodyRepositoryImpl extends EntityRepositoryImpl<CorporateBodyImpl>
        implements CorporateBodyRepository<CorporateBodyImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodyRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_CB
          = " c.uuid cb_uuid, c.refid cb_refId, c.label cb_label, c.description cb_description,"
          + " c.identifiable_type cb_type, c.entity_type cb_entityType,"
          + " c.created cb_created, c.last_modified cb_lastModified,"
          + " c.preview_hints cb_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_CB = SQL_REDUCED_FIELDS_CB + ", c.text cb_text, c.homepage_url cb_homepageUrl";

  public static final String TABLE_NAME = "corporatebodies";

  @Autowired
  public CorporateBodyRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
            dbi,
            identifierRepository,
            TABLE_NAME,
            "c",
            "cb",
            CorporateBodyImpl.class,
            SQL_REDUCED_FIELDS_CB,
            SQL_FULL_FIELDS_CB);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "lastModified", "refId"};
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
  public CorporateBodyImpl save(CorporateBodyImpl corporateBody) {
    corporateBody.setUuid(UUID.randomUUID());
    corporateBody.setCreated(LocalDateTime.now());
    corporateBody.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid
            = corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query
            = "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, homepage_url"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :homepageUrl"
            + ")";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(corporateBody)
                    .execute());

    // save identifiers
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBodyImpl result = findOne(corporateBody.getUuid());
    return result;
  }

  @Override
  public CorporateBodyImpl update(CorporateBodyImpl corporateBody) {
    corporateBody.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid
            = corporateBody.getPreviewImage() == null ? null : corporateBody.getPreviewImage().getUuid();

    String query
            = "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, homepage_url=:homepageUrl"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(corporateBody)
                    .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(corporateBody);
    Set<Identifier> identifiers = corporateBody.getIdentifiers();
    saveIdentifiers(identifiers, corporateBody);

    CorporateBodyImpl result = findOne(corporateBody.getUuid());
    return result;
  }
}
