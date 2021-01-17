package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Repository for Project persistence handling. */
public interface ProjectRepository<P extends Project> extends EntityRepository<P> {

  default boolean addDigitalObject(P project, DigitalObject digitalObject) {
    if (project == null || digitalObject == null) {
      return false;
    }
    return addDigitalObjects(project.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(P project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return addDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);

  default PageResponse<DigitalObject> getDigitalObjects(Project project, PageRequest pageRequest) {
    if (project == null) {
      return null;
    }
    return getDigitalObjects(project.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest);

  default boolean removeDigitalObject(P project, DigitalObject digitalObject) {
    if (project == null || digitalObject == null) {
      return false;
    }
    return removeDigitalObject(project.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid);

  default boolean saveDigitalObjects(P project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);

  List<P> getAll();

  boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid);
}
