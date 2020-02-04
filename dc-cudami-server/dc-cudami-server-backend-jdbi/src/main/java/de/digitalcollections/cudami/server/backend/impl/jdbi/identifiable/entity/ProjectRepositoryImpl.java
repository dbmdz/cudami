package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepositoryImpl extends EntityRepositoryImpl<Project>
    implements ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT p.uuid p_uuid, p.refid p_refId, p.label p_label, p.description p_description,"
          + " p.identifiable_type p_type, p.entity_type p_entityType,"
          + " p.created p_created, p.last_modified p_lastModified,"
          + " p.text p_text, p.start_date p_startDate, p.end_date p_endDate,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM projects as p"
          + " LEFT JOIN identifiers as id on p.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on p.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT p.uuid p_uuid, p.refid p_refId, p.label p_label, p.description p_description,"
          + " p.identifiable_type p_type, p.entity_type p_entityType,"
          + " p.created p_created, p.last_modified p_lastModified,"
          + " p.start_date p_startDate, p.end_date p_endDate,"
          + " file.uri f_uri, file.filename f_filename"
          + " FROM projects as p"
          + " LEFT JOIN fileresources_image as file on p.previewfileresource = file.uuid";

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
  public PageResponse<Project> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ProjectImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ProjectImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, ProjectImpl.class, "p_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Project findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE p.uuid = :uuid";

    Optional<ProjectImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ProjectImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ProjectImpl.class, "p_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public Project findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ProjectImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ProjectImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ProjectImpl.class, "p_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"p.created", "p.last_modified", "p.refid"};
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
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, start_date, end_date"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
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
    List<Identifier> identifiers = project.getIdentifiers();
    saveIdentifiers(identifiers, project);

    Project result = findOne(project.getUuid());
    return result;
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
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
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
    List<Identifier> identifiers = project.getIdentifiers();
    saveIdentifiers(identifiers, project);

    Project result = findOne(project.getUuid());
    return result;
  }
}
