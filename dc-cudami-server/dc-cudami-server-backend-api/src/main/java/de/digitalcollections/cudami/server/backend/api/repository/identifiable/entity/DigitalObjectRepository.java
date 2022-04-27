package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

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

/** Repository for Digital object persistence handling. */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  void deleteFileResources(UUID digitalObjectUuid);

  default SearchPageResponse<Collection> findCollections(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    return findCollections(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Collection> findCollections(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest);

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

  default List<FileResource> getRenderingFileResources(DigitalObject digitalObject) {
    return getRenderingFileResources(digitalObject.getUuid());
  }

  List<FileResource> getRenderingFileResources(UUID digitalObjectUuid);

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
    return findProjects(digitalObject.getUuid(), searchPageRequest);
  }

  SearchPageResponse<Project> findProjects(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest);

  default List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return setFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  default List<FileResource> saveRenderingResources(
      DigitalObject digitalObject, List<FileResource> renderingResources) {
    if (renderingResources == null) {
      return null;
    }
    return setRenderingResources(digitalObject.getUuid(), renderingResources);
  }

  List<FileResource> setRenderingResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources);

  default List<LinkedDataFileResource> saveLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources) {
    if (linkedDataFileResources == null) {
      return null;
    }

    return setLinkedDataFileResources(digitalObject.getUuid(), linkedDataFileResources);
  }

  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources);
}
