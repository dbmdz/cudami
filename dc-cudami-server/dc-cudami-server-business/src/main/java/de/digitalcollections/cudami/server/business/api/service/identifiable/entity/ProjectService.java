package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectService extends EntityService<Project> {

  boolean addDigitalObject(Project project, DigitalObject digitalObject);

  boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects);

  PageResponse<DigitalObject> getDigitalObjects(Project project, PageRequest pageRequest);

  boolean removeDigitalObject(Project project, DigitalObject digitalObject);

  boolean saveDigitalObjects(Project project, List<DigitalObject> digitalObjects);

  void delete(UUID uuid);

  List<Project> getAll();
}
