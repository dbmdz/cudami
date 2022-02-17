package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
import org.springframework.util.StringUtils;

@Repository
public class ProjectRepositoryImpl extends EntityRepositoryImpl<Project>
    implements ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "pr";
  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "projects";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields() + ", end_date, start_date, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues() + ", :endDate, :startDate, :text::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", end_date=:endDate, start_date=:startDate, text=:text::JSONB";
  }

  @Lazy @Autowired private DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  @Autowired
  public ProjectRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Project.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        cudamiConfig.getOffsetForAlternativePaging());
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
  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID projectUuid, SearchPageRequest searchPageRequest) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + doTableName
            + " AS "
            + doTableAlias
            + " INNER JOIN project_digitalobjects AS pd ON "
            + doTableAlias
            + ".uuid = pd.digitalobject_uuid"
            + " WHERE pd.project_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", projectUuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(doTableAlias);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT pd.sortindex AS idx, *" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = null;
    if (searchPageRequest.getSorting() == null) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY pd.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
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
    super.save(project);
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
    super.update(project);
    Project result = findOne(project.getUuid());
    return result;
  }
}
