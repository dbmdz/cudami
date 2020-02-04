package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepositoryImpl extends EntityRepositoryImpl<Project>
    implements ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

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
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified, text FROM projects");
    addPageRequestParams(pageRequest, query);

    List<ProjectImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(ProjectImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Project findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified, text FROM projects WHERE uuid = :uuid";
    Project project =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(ProjectImpl.class)
                    .findOne()
                    .orElse(null));
    return project;
  }

  @Override
  public Project findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public Project save(Project project) {
    project.setUuid(UUID.randomUUID());
    project.setCreated(LocalDateTime.now());
    project.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO projects("
            + "uuid, label, description, identifiable_type, entity_type, created, last_modified, text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified, :text::JSONB"
            + ") RETURNING *";
    Project result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(project)
                    .mapToBean(ProjectImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Project update(Project project) {
    project.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type

    String query =
        "UPDATE projects SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified, text=:text::JSONB"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    Project result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(project)
                    .mapToBean(ProjectImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
