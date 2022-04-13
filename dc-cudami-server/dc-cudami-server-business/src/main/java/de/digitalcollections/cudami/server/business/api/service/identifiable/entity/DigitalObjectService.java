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

  SearchPageResponse<Collection> getActiveCollections(
      DigitalObject digitalObject, SearchPageRequest pageRequest);

  default SearchPageResponse<Collection> getCollections(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    return getCollections(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Collection> getCollections(
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

  default SearchPageResponse<Project> getProjects(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    return getProjects(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Project> getProjects(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest);

  default List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  default List<FileResource> saveRenderingResources(
      DigitalObject digitalObject, List<FileResource> renderingResources)
      throws CudamiServiceException {
    if (renderingResources == null) {
      return null;
    }
    return saveRenderingResources(digitalObject.getUuid(), renderingResources);
  }

  List<FileResource> saveRenderingResources(
      UUID digitalObjectUuid, List<FileResource> renderingFileResources);

  default List<LinkedDataFileResource> saveLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources) {
    if (linkedDataFileResources == null) {
      return null;
    }
    return saveLinkedDataFileResources(digitalObject.getUuid(), linkedDataFileResources);
  }

  List<LinkedDataFileResource> saveLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources);
}
