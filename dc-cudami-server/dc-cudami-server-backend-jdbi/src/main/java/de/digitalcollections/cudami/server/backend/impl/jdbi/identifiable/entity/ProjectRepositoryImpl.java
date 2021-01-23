package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepositoryImpl extends EntityRepositoryImpl<Project>
    implements ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "pr";

  public static final String SQL_REDUCED_FIELDS_PR =
      " p.uuid pr_uuid, p.refid pr_refId, p.label pr_label, p.description pr_description,"
          + " p.identifiable_type pr_type, p.entity_type pr_entityType,"
          + " p.created pr_created, p.last_modified pr_lastModified,"
          + " p.start_date pr_startDate, p.end_date pr_endDate,"
          + " p.preview_hints pr_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_PR = SQL_REDUCED_FIELDS_PR + ", p.text pr_text";

  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "projects";

  @Lazy @Autowired private DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  @Autowired
  public ProjectRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        ProjectImpl.class,
        SQL_REDUCED_FIELDS_PR,
        SQL_FULL_FIELDS_PR);
  }

  @Override
  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    if (projectUuid != null && digitalObjects != null) {
      // get max sortIndex of existing
      Integer nextSortIndex =
          retrieveNextSortIndexForParentChildren(
              dbi, "project_digitalobjects", "project_uuid", projectUuid);

      // save relation to project
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO project_digitalobjects(project_uuid, digitalobject_uuid, sortIndex) VALUES (:uuid, :digitalObjectUuid, :sortIndex) ON CONFLICT (project_uuid, digitalobject_uuid) DO NOTHING");
            digitalObjects.forEach(
                digitalObject -> {
                  preparedBatch
                      .bind("uuid", projectUuid)
                      .bind("digitalObjectUuid", digitalObject.getUuid())
                      .bind("sortIndex", nextSortIndex + getIndex(digitalObjects, digitalObject))
                      .add();
                });
            preparedBatch.execute();
          });
      return true;
    }
    return false;
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
        return "p.created";
      case "lastModified":
        return "p.last_modified";
      case "refId":
        return "p.refid";
      default:
        return null;
    }
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + doTableName
            + " AS "
            + doTableAlias
            + " LEFT JOIN project_digitalobjects AS pd ON "
            + doTableAlias
            + ".uuid = pd.digitalobject_uuid"
            + " WHERE pd.project_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY pd.sortIndex ASC");
    addPageRequestParams(pageRequest, innerQuery);

    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            DigitalObjectRepositoryImpl.SQL_REDUCED_FIELDS_DO,
            innerQuery,
            Map.of("uuid", projectUuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", projectUuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid) {
    if (projectUuid != null && digitalObjectUuid != null) {
      // delete relation to project
      String query =
          "DELETE FROM project_digitalobjects WHERE project_uuid=:projectUuid AND digitalobject_uuid=:digitalObjectUuid";

      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("projectUuid", projectUuid)
                  .bind("digitalObjectUuid", digitalObjectUuid)
                  .execute());
      return true;
    }
    return false;
  }

  @Override
  public boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid) {
    if (digitalObjectUuid == null) {
      return false;
    }

    String query = "DELETE FROM project_digitalobjects WHERE digitalobject_uuid=:digitalObjectUuid";

    dbi.withHandle(
        h -> h.createUpdate(query).bind("digitalObjectUuid", digitalObjectUuid).execute());
    return true;
  }

  @Override
  public Project save(Project project) {
    project.setUuid(UUID.randomUUID());
    project.setCreated(LocalDateTime.now());
    project.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        project.getPreviewImage() == null ? null : project.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, start_date, end_date"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :startDate, :endDate"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(project)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = project.getIdentifiers();
    saveIdentifiers(identifiers, project);

    Project result = findOne(project.getUuid());
    return result;
  }

  @Override
  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM project_digitalobjects WHERE project_uuid = :uuid")
                .bind("uuid", projectUuid)
                .execute());

    if (digitalObjects != null) {
      // save relation to project
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO project_digitalobjects(project_uuid, digitalobject_uuid, sortIndex) VALUES (:uuid, :digitalObjectUuid, :sortIndex)");
            for (DigitalObject digitalObject : digitalObjects) {
              preparedBatch
                  .bind("uuid", projectUuid)
                  .bind("digitalObjectUuid", digitalObject.getUuid())
                  .bind("sortIndex", getIndex(digitalObjects, digitalObject))
                  .add();
            }
            preparedBatch.execute();
          });
      return true;
    }
    return false;
  }

  @Override
  public Project update(Project project) {
    project.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        project.getPreviewImage() == null ? null : project.getPreviewImage().getUuid();

    final String sql =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, start_date=:startDate, end_date=:endDate"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(project)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(project);
    Set<Identifier> identifiers = project.getIdentifiers();
    saveIdentifiers(identifiers, project);

    Project result = findOne(project.getUuid());
    return result;
  }
}
