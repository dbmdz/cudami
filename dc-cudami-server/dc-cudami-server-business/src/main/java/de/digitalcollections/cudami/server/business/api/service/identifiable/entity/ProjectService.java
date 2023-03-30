package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;

public interface ProjectService extends EntityService<Project> {

  boolean addDigitalObject(Project project, DigitalObject digitalObject) throws ServiceException;

  boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws ServiceException;

  PageResponse<DigitalObject> findDigitalObjects(Project project, PageRequest pageRequest)
      throws ServiceException;

  boolean removeDigitalObject(Project project, DigitalObject digitalObject) throws ServiceException;

  boolean removeDigitalObjectFromAllProjects(DigitalObject digitalObject) throws ServiceException;

  boolean setDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws ServiceException;
}
