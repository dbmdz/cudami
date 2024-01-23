package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  void deleteFileResources(DigitalObject digitalObject) throws ServiceException;

  PageResponse<Collection> findActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest) throws ServiceException;

  PageResponse<Collection> findCollections(DigitalObject digitalObject, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<Project> findProjects(DigitalObject digitalObject, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<DigitalObject> findDigitalObjectsByItem(Item item, PageRequest pageRequest)
      throws ServiceException;

  @Override
  default DigitalObject getByExample(DigitalObject digitalObject) throws ServiceException {
    return getByExample(digitalObject, false);
  }

  DigitalObject getByExample(DigitalObject digitalObject, boolean fillWemi) throws ServiceException;

  @Override
  default DigitalObject getByIdentifier(Identifier identifier) throws ServiceException {
    return getByIdentifier(identifier, false);
  }

  List<DigitalObject> getByExamples(List<DigitalObject> digitalObjects, boolean fillWemi)
      throws ServiceException;

  DigitalObject getByIdentifier(Identifier identifier, boolean fillWemi) throws ServiceException;

  List<FileResource> getFileResources(DigitalObject digitalObject) throws ServiceException;

  List<ImageFileResource> getIiifImageFileResources(DigitalObject buildExampleWithUuid)
      throws ServiceException;

  List<ImageFileResource> getImageFileResources(DigitalObject digitalObject)
      throws ServiceException;

  Item getItem(DigitalObject digitalObject) throws ServiceException;

  List<Locale> getLanguagesOfCollections(DigitalObject digitalObject) throws ServiceException;

  List<Locale> getLanguagesOfContainedDigitalObjects(DigitalObject digitalObject)
      throws ServiceException;

  List<Locale> getLanguagesOfProjects(DigitalObject digitalObject) throws ServiceException;

  List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject)
      throws ServiceException;

  List<FileResource> getRenderingFileResources(DigitalObject digitalObject) throws ServiceException;

  List<FileResource> setFileResources(DigitalObject digitalObject, List<FileResource> fileResources)
      throws ServiceException, ValidationException;

  boolean setItem(DigitalObject digitalObject, Item item)
      throws ConflictException, ValidationException, ServiceException;

  void setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException, ValidationException;

  void setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws ServiceException;
}
