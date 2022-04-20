package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for Digital Object handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject>
    implements DigitalObjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectServiceImpl.class);

  private final CollectionService collectionService;
  private final DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService;
  private final LinkedDataFileResourceService linkedDataFileResourceService;
  private final ProjectService projectService;

  public DigitalObjectServiceImpl(
      DigitalObjectRepository repository,
      CollectionService collectionService,
      ProjectService projectService,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      LinkedDataFileResourceService linkedDataFileResourceService,
      DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierRepository,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
    this.collectionService = collectionService;
    this.projectService = projectService;
    this.digitalObjectRenderingFileResourceService = digitalObjectRenderingFileResourceService;
    this.linkedDataFileResourceService = linkedDataFileResourceService;
  }

  @Override
  public boolean delete(UUID uuid) {
    // Check for existance. If not given, return false.
    DigitalObject existingDigitalObject = getByUuid(uuid);
    if (existingDigitalObject == null) {
      return false;
    }

    // Remove connection to collections
    collectionService.removeDigitalObjectFromAllCollections(existingDigitalObject);

    // Remove connection to projects
    projectService.removeDigitalObjectFromAllProjects(existingDigitalObject);

    // Remove preview images
    deleteFileResources(existingDigitalObject.getUuid());

    // Remove identifiers
    deleteIdentifiers(existingDigitalObject.getUuid());

    // Remove the digitalObject itself
    repository.delete(uuid);

    return true;
  }

  @Override
  public void deleteFileResources(UUID digitalObjectUuid) {
    ((DigitalObjectRepository) repository).deleteFileResources(digitalObjectUuid);
  }

  @Override
  public SearchPageResponse<Collection> getActiveCollections(
      DigitalObject digitalObject, SearchPageRequest searchPageRequest) {
    Filtering filtering = filteringForActive();
    searchPageRequest.add(filtering);
    return ((DigitalObjectRepository) repository).getCollections(digitalObject, searchPageRequest);
  }

  @Override
  public SearchPageResponse<Collection> getCollections(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest) {
    return ((DigitalObjectRepository) repository)
        .getCollections(digitalObjectUuid, searchPageRequest);
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getFileResources(digitalObjectUuid);
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getImageFileResources(digitalObjectUuid);
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid) {
    return linkedDataFileResourceService.getLinkedDataFileResourcesForDigitalObjectUuid(
        digitalObjectUuid);
  }

  @Override
  public List<FileResource> getRenderingResources(UUID digitalObjectUuid) {
    return digitalObjectRenderingFileResourceService.getForDigitalObject(digitalObjectUuid);
  }

  @Override
  public Item getItem(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getItem(digitalObjectUuid);
  }

  @Override
  public List<Locale> getLanguagesOfCollections(UUID uuid) {
    return ((DigitalObjectRepository) this.repository).getLanguagesOfCollections(uuid);
  }

  @Override
  public List<Locale> getLanguagesOfProjects(UUID uuid) {
    return ((DigitalObjectRepository) this.repository).getLanguagesOfProjects(uuid);
  }

  @Override
  public SearchPageResponse<Project> getProjects(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest) {
    return ((DigitalObjectRepository) repository).getProjects(digitalObjectUuid, searchPageRequest);
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) {
    return ((DigitalObjectRepository) repository)
        .saveFileResources(digitalObjectUuid, fileResources);
  }

  @Override
  public List<FileResource> saveRenderingResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources) {
    return digitalObjectRenderingFileResourceService.saveForDigitalObject(
        digitalObjectUuid, renderingResources);
  }

  @Override
  public List<LinkedDataFileResource> saveLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources) {
    return linkedDataFileResourceService.saveLinkedDataFileResources(
        digitalObjectUuid, linkedDataFileResources);
  }

  @Override
  public DigitalObject getByIdentifier(Identifier identifier) {
    return fillDigitalObject(super.getByIdentifier(identifier));
  }

  @Override
  public DigitalObject getByUuidAndLocale(UUID uuid, Locale locale)
      throws IdentifiableServiceException {
    return fillDigitalObject(super.getByUuidAndLocale(uuid, locale));
  }

  @Override
  public DigitalObject getByRefId(long refId) {
    return fillDigitalObject(super.getByRefId(refId));
  }

  @Override
  public List<DigitalObject> getRandom(int count) {
    List<DigitalObject> digitalObjects = super.getRandom(count);
    if (digitalObjects == null || digitalObjects.isEmpty()) {
      return digitalObjects;
    }
    return digitalObjects.stream().map(this::fillDigitalObject).collect(Collectors.toList());
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject)
      throws IdentifiableServiceException, ValidationException {
    // Keep the resources for later saving, because the repository save
    // method returns the DigitalObject with empty fields there!
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();

    digitalObject = super.save(digitalObject);

    // save the linked data resources
    saveLinkedDataFileResources(digitalObject, linkedDataResources);

    // save the rendering resources
    try {
      saveRenderingResources(digitalObject, renderingResources);
    } catch (CudamiServiceException e) {
      throw new IdentifiableServiceException("Cannot update DigitalObject: " + e, e);
    }

    return fillDigitalObject(digitalObject);
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject)
      throws IdentifiableServiceException, ValidationException {
    // Keep the resources for later saving, because the repository save
    // method returns the DigitalObject with empty fields there!
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();

    digitalObject = super.update(digitalObject);

    // save the linked data resources
    saveLinkedDataFileResources(digitalObject, linkedDataResources);

    // save the rendering resources
    try {
      saveRenderingResources(digitalObject, renderingResources);
    } catch (CudamiServiceException e) {
      throw new IdentifiableServiceException("Cannot update DigitalObject: " + e, e);
    }

    return fillDigitalObject(digitalObject);
  }

  @Override
  public List<FileResource> getRenderingResources(DigitalObject digitalObject)
      throws CudamiServiceException {
    return digitalObjectRenderingFileResourceService.getForDigitalObject(digitalObject);
  }

  @Override
  public List<FileResource> saveRenderingResources(
      DigitalObject digitalObject, List<FileResource> renderingResources)
      throws CudamiServiceException {
    return digitalObjectRenderingFileResourceService.saveForDigitalObject(
        digitalObject, renderingResources);
  }

  private DigitalObject fillDigitalObject(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return null;
    }

    // Look for linked data file resources. If they exist, fill the DigitalObject
    List<LinkedDataFileResource> linkedDataFileResources =
        getLinkedDataFileResources(digitalObject.getUuid());
    if (linkedDataFileResources != null && !linkedDataFileResources.isEmpty()) {
      digitalObject.setLinkedDataResources(new ArrayList<>(linkedDataFileResources));
    }

    // Look for rendering resources. If they exist, fill the object
    List<FileResource> renderingResources = getRenderingResources(digitalObject.getUuid());
    if (renderingResources != null && !renderingResources.isEmpty()) {
      digitalObject.setRenderingResources(new ArrayList<>(renderingResources));
    }

    return digitalObject;
  }
}
