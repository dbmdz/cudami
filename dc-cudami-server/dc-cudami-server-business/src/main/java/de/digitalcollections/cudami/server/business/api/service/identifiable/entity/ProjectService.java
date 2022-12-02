package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface ProjectService extends EntityService<Project> {

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

  default PageResponse<DigitalObject> findDigitalObjects(Project project, PageRequest pageRequest) {
    if (project == null) {
      return null;
    }
    return findDigitalObjects(project.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjects(UUID projectUuid, PageRequest pageRequest);

  default boolean removeDigitalObject(Project project, DigitalObject digitalObject) {
    if (project == null || digitalObject == null) {
      return false;
    }
    return removeDigitalObject(project.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid);

  default boolean removeDigitalObjectFromAllProjects(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return false;
    }
    return removeDigitalObjectFromAllProjects(digitalObject.getUuid());
  }

  boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid);

  default boolean setDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return setDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);
}
