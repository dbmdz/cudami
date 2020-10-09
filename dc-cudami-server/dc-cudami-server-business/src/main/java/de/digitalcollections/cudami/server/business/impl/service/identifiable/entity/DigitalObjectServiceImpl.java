package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Digital Object handling. */
@Service
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject>
    implements DigitalObjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectServiceImpl.class);

  private final CollectionService collectionService;
  private final ProjectService projectService;

  @Autowired
  public DigitalObjectServiceImpl(
      DigitalObjectRepository repository,
      CollectionService collectionService,
      ProjectService projectService) {
    super(repository);
    this.collectionService = collectionService;
    this.projectService = projectService;
  }

  @Override
  public boolean delete(UUID uuid) {
    // Check for existance. If not given, return false.
    DigitalObject existingDigitalObject = get(uuid);
    if (existingDigitalObject == null) {
      return false;
    }

    // Remove connection to collections
    collectionService.removeDigitalObjectFromAllCollections(existingDigitalObject);

    // Remove connection to projects
    projectService.removeDigitalObjectFromAllProjects(existingDigitalObject);

    // Remove preview images
    ((DigitalObjectRepository) repository).deleteFileResources(existingDigitalObject.getUuid());

    // Remove identifiers
    ((DigitalObjectRepository) repository).deleteIdentifiers(existingDigitalObject.getUuid());

    // Remove the digitalObject itself
    repository.delete(uuid);

    return true;
  }

  @Override
  public List<DigitalObject> findAllReduced() {
    return ((EntityRepository) repository).findAllReduced(EntityType.DIGITAL_OBJECT);
  }

  @Override
  public PageResponse<Collection> getActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return ((DigitalObjectRepository) repository).getCollections(digitalObject, pageRequest);
  }

  @Override
  public DigitalObject getByIdentifier(String namespace, String id) {
    return ((DigitalObjectRepository) repository).findByIdentifier(namespace, id);
  }

  @Override
  public PageResponse<Collection> getCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).getCollections(digitalObject, pageRequest);
  }

  @Override
  public List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getFileResources(digitalObjectUuid);
  }

  @Override
  public List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getImageFileResources(digitalObjectUuid);
  }

  @Override
  public PageResponse<Project> getProjects(DigitalObject digitalObject, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).getProjects(digitalObject, pageRequest);
  }

  Identifier getidentifer(DigitalObject digitalObject, String name) {
    for (Identifier identifier : digitalObject.getIdentifiers()) {
      if (name.equals(identifier.getNamespace())) {
        return identifier;
      }
    }
    return null;
  }

  @Override
  public List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) {
    return ((DigitalObjectRepository) repository)
        .saveFileResources(digitalObjectUuid, fileResources);
  }
}
