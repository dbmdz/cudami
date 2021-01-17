package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepositoryImpl extends EntityRepositoryImpl<ProjectImpl>
    implements ProjectRepository<ProjectImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_PR = "";
  public static final String SQL_FULL_FIELDS_PR = SQL_REDUCED_FIELDS_PR + "";

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT p.uuid p_uuid, p.refid p_refId, p.label p_label, p.description p_description,"
          + " p.identifiable_type p_type, p.entity_type p_entityType,"
          + " p.created p_created, p.last_modified p_lastModified,"
          + " p.text p_text, p.start_date p_startDate, p.end_date p_endDate,"
          + " p.preview_hints p_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM projects as p"
          + " LEFT JOIN identifiers as id on p.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on p.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT p.uuid p_uuid, p.refid p_refId, p.label p_label, p.description p_description,"
          + " p.identifiable_type p_type, p.entity_type p_entityType,"
          + " p.created p_created, p.last_modified p_lastModified,"
          + " p.start_date p_startDate, p.end_date p_endDate,"
          + " p.preview_hints p_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM projects as p"
          + " LEFT JOIN fileresources_image as file on p.previewfileresource = file.uuid";

  // select to retrieve the whole table including referenced identifiers. Use this with caution!
  private static final String FIND_ALL_SQL = FIND_ONE_BASE_SQL;

  @Autowired
  public ProjectRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM projects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public List<Project> getAll() {
    List<Project> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(FIND_ALL_SQL)
                        .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ProjectImpl>(),
                            (map, rowView) -> {
                              ProjectImpl project =
                                  map.computeIfAbsent(
                                      rowView.getColumn("p_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ProjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                project.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                project.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .map(Project.class::cast)
            .collect(Collectors.toList());

    return result;
  }

  @Override
  public PageResponse<Project> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ProjectImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ProjectImpl>(),
                            (map, rowView) -> {
                              ProjectImpl project =
                                  map.computeIfAbsent(
                                      rowView.getColumn("p_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ProjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                project.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Project findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE p.uuid = :uuid";

    ProjectImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ProjectImpl>(),
                            (map, rowView) -> {
                              ProjectImpl project =
                                  map.computeIfAbsent(
                                      rowView.getColumn("p_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ProjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                project.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                project.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public Project findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ProjectImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ProjectImpl>(),
                            (map, rowView) -> {
                              ProjectImpl project =
                                  map.computeIfAbsent(
                                      rowView.getColumn("p_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ProjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                project.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                project.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest) {
    final String baseQuery =
        "SELECT d.uuid d_uuid, d.label d_label, d.refid d_refId,"
            + " d.created d_created, d.last_modified d_lastModified, d.custom_attrs d_customAttributes,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM digitalobjects as d"
            + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
            + " LEFT JOIN project_digitalobjects as pd on d.uuid = pd.digitalobject_uuid"
            + " WHERE pd.project_uuid = :uuid"
            + " ORDER BY pd.sortIndex ASC";
    StringBuilder query = new StringBuilder(baseQuery);

    // we add fix sorting in above query; otherwise we get in conflict with allowed sorting
    // and column names of this repository (it is for projects, not sublists of
    // digitalobjects...)
    pageRequest.setSorting(null);
    addPageRequestParams(pageRequest, query);

    List<DigitalObject> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query.toString())
                    .bind("uuid", projectUuid)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, DigitalObjectImpl>(),
                        (map, rowView) -> {
                          DigitalObjectImpl digitalObject =
                              map.computeIfAbsent(
                                  rowView.getColumn("d_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(DigitalObjectImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            digitalObject.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            digitalObject.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(DigitalObject.class::cast)
                    .collect(Collectors.toList()));
    String countQuery =
        "SELECT count(*) FROM digitalobjects as d"
            + " LEFT JOIN project_digitalobjects as pd on d.uuid = pd.digitalobject_uuid"
            + " WHERE pd.project_uuid = :uuid";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("uuid", projectUuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    PageResponse<DigitalObject> pageResponse = new PageResponseImpl<>(result, pageRequest, total);
    return pageResponse;
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
        "INSERT INTO projects("
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
  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    if (projectUuid != null && digitalObjects != null) {
      // get max sortIndex of existing
      Integer nextSortIndex =
          selectNextSortIndexForParentChildren(
              dbi, "project_digitalobjects", "project_uuid", projectUuid);

      // save relation to project
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO project_digitalobjects(project_uuid, digitalobject_uuid, sortIndex) VALUES (:uuid, :digitalObjectUuid, :sortIndex) ON CONFLICT (project_uuid, digitalobject_uuid) DO NOTHING");
            for (DigitalObject digitalObject : digitalObjects) {
              preparedBatch
                  .bind("uuid", projectUuid)
                  .bind("digitalObjectUuid", digitalObject.getUuid())
                  .bind("sortIndex", nextSortIndex + getIndex(digitalObjects, digitalObject))
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

    String query =
        "UPDATE projects SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, start_date=:startDate, end_date=:endDate"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(project)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(project);
    Set<Identifier> identifiers = project.getIdentifiers();
    saveIdentifiers(identifiers, project);

    Project result = findOne(project.getUuid());
    return result;
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
}
