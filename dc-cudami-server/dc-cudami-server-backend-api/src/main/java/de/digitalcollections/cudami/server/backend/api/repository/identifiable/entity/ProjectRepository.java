package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Repository for Project persistence handling. */
public interface ProjectRepository extends EntityRepository<Project> {

  default boolean addDigitalObject(Project project, DigitalObject digitalObject) {
    if (project == null || digitalObject == null) {
      return false;
    }
    return addDigitalObjects(project.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return addDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);

  default SearchPageResponse<DigitalObject> getDigitalObjects(
      Project project, SearchPageRequest searchPageRequest) {
    if (project == null) {
      return null;
    }
    return findDigitalObjects(project.getUuid(), searchPageRequest);
  }

  SearchPageResponse<DigitalObject> findDigitalObjects(
      UUID projectUuid, SearchPageRequest searchPageRequest);

  default boolean removeDigitalObject(Project project, DigitalObject digitalObject) {
    if (project == null || digitalObject == null) {
      return false;
    }
    return removeDigitalObject(project.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid);

  boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid);

  default boolean saveDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return setDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);
}
