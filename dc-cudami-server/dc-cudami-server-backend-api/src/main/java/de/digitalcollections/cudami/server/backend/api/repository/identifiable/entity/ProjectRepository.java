package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Repository for Project persistence handling. */
public interface ProjectRepository extends EntityRepository<Project> {

  default boolean addDigitalObject(Project project, DigitalObject digitalObject)
      throws RepositoryException {
    if (project == null || digitalObject == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addDigitalObjects(project.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws RepositoryException {
    if (project == null || digitalObjects == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException;

  default PageResponse<DigitalObject> findDigitalObjects(Project project, PageRequest pageRequest)
      throws RepositoryException {
    if (project == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findDigitalObjects(project.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default boolean removeDigitalObject(Project project, DigitalObject digitalObject)
      throws RepositoryException {
    if (project == null || digitalObject == null) {
      throw new IllegalArgumentException("remove failed: given objects must not be null");
    }
    return removeDigitalObject(project.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid) throws RepositoryException;

  default boolean removeDigitalObjectFromAllProjects(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("remove failed: given object must not be null");
    }
    return removeDigitalObjectFromAllProjects(digitalObject.getUuid());
  }

  boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid) throws RepositoryException;

  default boolean setDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws RepositoryException {
    if (project == null || digitalObjects == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException;
}
