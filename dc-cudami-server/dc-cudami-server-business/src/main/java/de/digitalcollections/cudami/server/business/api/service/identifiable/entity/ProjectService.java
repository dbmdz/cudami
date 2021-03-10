package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

  default PageResponse<DigitalObject> getDigitalObjects(Project project, PageRequest pageRequest) {
    if (project == null) {
      return null;
    }
    return getDigitalObjects(project.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest);

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

  default boolean saveDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    if (project == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(project.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects);

  List<Locale> getLanguages();
}
