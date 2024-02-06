package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Digital object persistence handling. */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  default void deleteFileResources(DigitalObject digitalObject) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("delete failed: given object must not be null");
    }
    deleteFileResources(digitalObject.getUuid());
  }

  void deleteFileResources(UUID digitalObjectUuid) throws RepositoryException;

  default PageResponse<Collection> findCollections(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findCollections(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findFileResources(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<ImageFileResource> findImageFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findImageFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<ImageFileResource> findImageFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException;

  default PageResponse<Project> findProjects(DigitalObject digitalObject, PageRequest pageRequest)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findProjects(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<DigitalObject> findDigitalObjectsByItem(Item item, PageRequest pageRequest)
      throws RepositoryException {
    if (item == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findDigitalObjectsByItem(item.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjectsByItem(UUID itemUuid, PageRequest pageRequest)
      throws RepositoryException;

  default List<FileResource> getFileResources(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getFileResources(digitalObject.getUuid());
  }

  // FIXME: replace with pagerequest method
  List<FileResource> getFileResources(UUID digitalObjectUuid) throws RepositoryException;

  default List<ImageFileResource> getIiifImageFileResources(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getIiifImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getIiifImageFileResources(UUID uuid) throws RepositoryException;

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getImageFileResources(digitalObject.getUuid());
  }

  // FIXME: replace with pagerequest method
  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) throws RepositoryException;

  default List<Locale> getLanguagesOfCollections(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLanguagesOfCollections(digitalObject.getUuid());
  }

  List<Locale> getLanguagesOfCollections(UUID uuid) throws RepositoryException;

  default List<Locale> getLanguagesOfContainedDigitalObjects(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLanguagesOfContainedDigitalObjects(digitalObject.getUuid());
  }

  List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid) throws RepositoryException;

  default List<Locale> getLanguagesOfProjects(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLanguagesOfProjects(digitalObject.getUuid());
  }

  List<Locale> getLanguagesOfProjects(UUID uuid) throws RepositoryException;

  default List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources)
      throws RepositoryException, ValidationException {
    if (digitalObject == null || fileResources == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID digitalObjectUuid, List<FileResource> fileResources)
      throws RepositoryException, ValidationException;
}
