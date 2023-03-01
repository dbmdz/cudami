package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
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
import java.util.UUID;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  boolean addItemToDigitalObject(Item item, UUID digitalObjectUuid)
      throws ConflictException, ValidationException, ServiceException;

  void deleteFileResources(UUID digitalObjectUuid);

  PageResponse<Collection> findActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest);

  default PageResponse<Collection> findCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    return findCollections(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest);

  default PageResponse<Project> findProjects(DigitalObject digitalObject, PageRequest pageRequest) {
    return findProjects(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest);

  DigitalObject getByIdentifierWithWEMI(String namespace, String id) throws ServiceException;

  DigitalObject getByUuidWithWEMI(UUID uuid) throws ServiceException;

  default List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  default Item getItem(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return null;
    }
    return getItem(digitalObject.getUuid());
  }

  Item getItem(UUID digitalObjectUuid);

  List<Locale> getLanguagesOfCollections(UUID uuid);

  List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid);

  List<Locale> getLanguagesOfProjects(UUID uuid);

  default List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject) {
    return getLinkedDataFileResources(digitalObject.getUuid());
  }

  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid);

  default List<FileResource> getRenderingResources(DigitalObject digitalObject)
      throws ServiceException {
    return getRenderingResources(digitalObject.getUuid());
  }

  List<FileResource> getRenderingResources(UUID digitalObjectUuid) throws ServiceException;

  default List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) throws ServiceException {
    if (fileResources == null) {
      return null;
    }
    return setFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID digitalObjectUuid, List<FileResource> fileResources)
      throws ServiceException;

  default List<LinkedDataFileResource> setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException {
    if (linkedDataFileResources == null) {
      return null;
    }
    return setLinkedDataFileResources(digitalObject.getUuid(), linkedDataFileResources);
  }

  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException;

  default List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws ServiceException {
    if (renderingFileResources == null) {
      return null;
    }
    return setRenderingFileResources(digitalObject.getUuid(), renderingFileResources);
  }

  List<FileResource> setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingFileResources) throws ServiceException;
}
