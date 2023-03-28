package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Digital object persistence handling. */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  void deleteFileResources(UUID digitalObjectUuid);

  default PageResponse<Collection> findCollections(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      return null;
    }
    return findCollections(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      return null;
    }
    return findFileResources(digitalObject.getUuid(), pageRequest);
  }

  // FIXME: replace with pagerequest method
  List<FileResource> findFileResources(UUID digitalObjectUuid) throws RepositoryException;

  PageResponse<FileResource> findFileResources(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<ImageFileResource> findImageFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    return findImageFileResources(digitalObject.getUuid(), pageRequest);
  }

  // FIXME: replace with pagerequest method
  List<ImageFileResource> findImageFileResources(UUID digitalObjectUuid) throws RepositoryException;

  PageResponse<ImageFileResource> findImageFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException;

  default PageResponse<Project> findProjects(DigitalObject digitalObject, PageRequest pageRequest)
      throws RepositoryException {
    if (digitalObject == null) {
      return null;
    }
    return findProjects(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException;

  List<Locale> getLanguagesOfCollections(UUID uuid);

  List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid);

  List<Locale> getLanguagesOfProjects(UUID uuid);

  default List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) throws RepositoryException {
    if (fileResources == null) {
      return null;
    }
    return setFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID digitalObjectUuid, List<FileResource> fileResources)
      throws RepositoryException;
}
