package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
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

  DigitalObject getByIdentifierWithWEMI(Identifier identifier) throws ServiceException;

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
      throws ServiceException;

  boolean setItem(DigitalObject digitalObject, Item item)
      throws ConflictException, ValidationException, ServiceException;

  List<LinkedDataFileResource> setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException;

  List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws ServiceException;
}
