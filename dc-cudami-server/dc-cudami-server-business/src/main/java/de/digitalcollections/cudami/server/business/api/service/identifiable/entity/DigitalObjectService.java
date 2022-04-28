package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  void deleteFileResources(UUID digitalObjectUuid);

  SearchPageResponse<Collection> findActiveCollections(
      DigitalObject digitalObject, SearchPageRequest pageRequest);

  default SearchPageResponse<Collection> findCollections(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    return findCollections(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Collection> findCollections(
      UUID digitalObjectUuid, SearchPageRequest pageRequest);

  default List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  default List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject) {
    return getLinkedDataFileResources(digitalObject.getUuid());
  }

  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid);

  default List<FileResource> getRenderingResources(DigitalObject digitalObject)
      throws CudamiServiceException {
    return getRenderingResources(digitalObject.getUuid());
  }

  List<FileResource> getRenderingResources(UUID digitalObjectUuid) throws CudamiServiceException;

  default Item getItem(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return null;
    }
    return getItem(digitalObject.getUuid());
  }

  Item getItem(UUID digitalObjectUuid);

  List<Locale> getLanguagesOfCollections(UUID uuid);

  List<Locale> getLanguagesOfProjects(UUID uuid);

  default SearchPageResponse<Project> findProjects(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    return findProjects(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Project> findProjects(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest);

  default List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return setFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  default List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws CudamiServiceException {
    if (renderingFileResources == null) {
      return null;
    }
    return setRenderingFileResources(digitalObject.getUuid(), renderingFileResources);
  }

  List<FileResource> setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingFileResources);

  default List<LinkedDataFileResource> setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources) {
    if (linkedDataFileResources == null) {
      return null;
    }
    return setLinkedDataFileResources(digitalObject.getUuid(), linkedDataFileResources);
  }

  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources);
}
