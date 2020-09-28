package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  PageResponse<Collection> getCollections(DigitalObject digitalObject, PageRequest pageRequest);

  List<FileResource> getFileResources(DigitalObject digitalObject);

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  List<ImageFileResource> getImageFileResources(DigitalObject digitalObject);

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  PageResponse<Project> getProjects(DigitalObject digitalObject, PageRequest pageRequest);

  List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  /**
   * Returns a list of all DigitalObjects, reduced to their identifiers and last modified date
   *
   * @return partially (see above) filled list of all DigitalObjects
   */
  List<DigitalObject> findAllReduced();

  boolean delete(UUID uuid);
}
