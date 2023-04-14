package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "projects";

  @Lazy @Autowired private DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  public ProjectRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Project.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException {
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
  public Project create() throws RepositoryException {
    return new Project();
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws RepositoryException {
    final String crossTableAlias = "xtable";

    final String digitalObjectTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String digitalObjectTableName = digitalObjectRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + digitalObjectTableName
                + " AS "
                + digitalObjectTableAlias
                + " INNER JOIN project_digitalobjects AS "
                + crossTableAlias
                + " ON "
                + digitalObjectTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".digitalobject_uuid"
                + " WHERE "
                + crossTableAlias
                + ".project_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", projectUuid);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (digitalobject) than this repository (project)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, digitalObjectRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy =
        digitalObjectRepositoryImpl.addCrossTablePagingAndSorting(
            pageRequest, innerQuery, crossTableAlias);
    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", end_date, start_date, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :endDate, :startDate, :text::JSONB";
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
        + ".end_date "
        + mappingPrefix
        + "_endDate, "
        + tableAlias
        + ".start_date "
        + mappingPrefix
        + "_startDate";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", end_date=:endDate, start_date=:startDate, text=:text::JSONB";
  }

  @Override
  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws RepositoryException {
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
  public boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid)
      throws RepositoryException {
    if (digitalObjectUuid == null) {
      return false;
    }

    String query = "DELETE FROM project_digitalobjects WHERE digitalobject_uuid=:digitalObjectUuid";

    dbi.withHandle(
        h -> h.createUpdate(query).bind("digitalObjectUuid", digitalObjectUuid).execute());
    return true;
  }

  @Override
  public boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException {
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
}
