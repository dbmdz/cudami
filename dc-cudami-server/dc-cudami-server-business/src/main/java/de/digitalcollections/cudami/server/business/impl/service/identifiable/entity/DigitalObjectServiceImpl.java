package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
  private final DigitalObjectLinkedDataFileResourceService
      digitalObjectLinkedDataFileResourceService;
  private final DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService;
  private final ItemService itemService;
  private final ProjectService projectService;

  public DigitalObjectServiceImpl(
      DigitalObjectRepository repository,
      CollectionService collectionService,
      ProjectService projectService,
      IdentifierService identifierService,
      ItemService itemService,
      UrlAliasService urlAliasService,
      DigitalObjectLinkedDataFileResourceService digitalObjectLinkedDataFileResourceService,
      DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
    this.collectionService = collectionService;
    this.itemService = itemService;
    this.projectService = projectService;
    this.digitalObjectRenderingFileResourceService = digitalObjectRenderingFileResourceService;
    this.digitalObjectLinkedDataFileResourceService = digitalObjectLinkedDataFileResourceService;
  }

  @Override
  public boolean addItem(UUID digitalObjectUuid, Item item)
      throws ConflictException, ValidationException, ServiceException {
    // If the item does not exist, return false
    if (item == null) {
      return false;
    }

    // Retrieve the DigitalObject
    DigitalObject digitalObject = getByUuid(digitalObjectUuid);
    if (digitalObject == null) {
      return false;
    }

    // Ensure, that the DigitalObject is either not connected with any item or already belongs to
    // the item
    Item digitalObjectItem = digitalObject.getItem();
    if (digitalObjectItem != null && digitalObjectItem.getUuid().equals(item.getUuid())) {
      return true; // nothing to do
    }
    if (digitalObjectItem != null && !digitalObjectItem.getUuid().equals(item.getUuid())) {
      LOGGER.warn(
          "Trying to connect DigitalObject "
              + digitalObjectUuid
              + " to item "
              + item.getUuid()
              + ", but it already belongs to item "
              + digitalObjectItem.getUuid());
      throw new ConflictException(
          "DigitalObject "
              + digitalObject.getUuid()
              + " already belongs to item "
              + digitalObject.getItem().getUuid());
    }

    digitalObject.setItem(item);
    update(digitalObject);
    return true;
  }

  @Override
  public boolean delete(UUID uuid) throws ServiceException, ConflictException {
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

    // Remove LinkedDataFileResources (relation, and, if possible, resource)
    try {
      deleteLinkedDatafileResources(existingDigitalObject.getUuid());
    } catch (ServiceException e) {
      throw new ServiceException(
          "Cannot remove LinkedDataFileResource from digitalObject with uuid="
              + existingDigitalObject.getUuid()
              + ": "
              + e,
          e);
    }

    // Remove RenderingResources (relation, and, if possible, resource)
    try {
      deleteRenderingResource(existingDigitalObject.getUuid());
    } catch (ServiceException e) {
      throw new ServiceException(
          "Cannot remove RenderingFileResource from digitalObject with uuid="
              + existingDigitalObject.getUuid()
              + ": "
              + e,
          e);
    }

    return super.deleteByUuid(uuid);
  }

  private void deleteRenderingResource(UUID digitalObjectUuid) throws ServiceException {
    digitalObjectRenderingFileResourceService.deleteRenderingFileResources(digitalObjectUuid);
  }

  private void deleteLinkedDatafileResources(UUID digitalObjectUuid) throws ServiceException {
    digitalObjectLinkedDataFileResourceService.deleteLinkedDataFileResources(digitalObjectUuid);
  }

  @Override
  public void deleteFileResources(UUID digitalObjectUuid) {
    ((DigitalObjectRepository) repository).deleteFileResources(digitalObjectUuid);
  }

  private void fillDigitalObject(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return;
    }

    // Look for linked data file resources. If they exist, fill the DigitalObject
    List<LinkedDataFileResource> linkedDataFileResources =
        getLinkedDataFileResources(digitalObject.getUuid());
    if (linkedDataFileResources != null && !linkedDataFileResources.isEmpty()) {
      digitalObject.setLinkedDataResources(new ArrayList<>(linkedDataFileResources));
    }

    // Look for rendering resources. If they exist, fill the object
    List<FileResource> renderingResources = getRenderingFileResources(digitalObject.getUuid());
    if (renderingResources != null && !renderingResources.isEmpty()) {
      digitalObject.setRenderingResources(new ArrayList<>(renderingResources));
    }
  }

  @Override
  public PageResponse<Collection> findActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return ((DigitalObjectRepository) repository).findCollections(digitalObject, pageRequest);
  }

  @Override
  public PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).findCollections(digitalObjectUuid, pageRequest);
  }

  @Override
  public PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).findProjects(digitalObjectUuid, pageRequest);
  }

  @Override
  public DigitalObject getByIdentifier(Identifier identifier) {
    DigitalObject digitalObject = super.getByIdentifier(identifier);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByIdentifier(String namespace, String id) {
    Identifier identifier = new Identifier(null, namespace, id);
    return getByIdentifier(identifier);
  }

  @Override
  public DigitalObject getByIdentifierWithWEMI(String namespace, String id)
      throws ServiceException {
    DigitalObject digitalObject = getByIdentifier(namespace, id);
    if (digitalObject == null) {
      return null;
    }
    if (digitalObject.getItem() != null) {
      Item item = itemService.getByUuid(digitalObject.getItem().getUuid());

      // for later:
      if (item.getManifestation() != null) {
        // TODO: fetch manifestation and fill item
      }

      digitalObject.setItem(item);
    }

    return digitalObject;
  }

  @Override
  public DigitalObject getByRefId(long refId) {
    DigitalObject digitalObject = super.getByRefId(refId);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByUuid(UUID uuid) throws ServiceException {
    DigitalObject digitalObject = super.getByUuid(uuid);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByUuidAndLocale(UUID uuid, Locale locale) throws ServiceException {
    DigitalObject digitalObject = super.getByUuidAndLocale(uuid, locale);
    fillDigitalObject(digitalObject);
    return digitalObject;
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
  public Item getItem(UUID digitalObjectUuid) {
    DigitalObject digitalObject = repository.getByUuid(digitalObjectUuid);
    if (digitalObject == null) {
      return null;
    }

    return digitalObject.getItem();
  }

  @Override
  public List<Locale> getLanguagesOfCollections(UUID uuid) {
    return ((DigitalObjectRepository) repository).getLanguagesOfCollections(uuid);
  }

  @Override
  public List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid) {
    return ((DigitalObjectRepository) repository).getLanguagesOfContainedDigitalObjects(uuid);
  }

  @Override
  public List<Locale> getLanguagesOfProjects(UUID uuid) {
    return ((DigitalObjectRepository) repository).getLanguagesOfProjects(uuid);
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid) {
    return digitalObjectLinkedDataFileResourceService.getLinkedDataFileResources(digitalObjectUuid);
  }

  @Override
  public List<DigitalObject> getRandom(int count) {
    List<DigitalObject> digitalObjects = super.getRandom(count);
    if (digitalObjects == null || digitalObjects.isEmpty()) {
      return digitalObjects;
    }
    digitalObjects.stream().forEach(this::fillDigitalObject);
    return digitalObjects;
  }

  @Override
  public List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) {
    return digitalObjectRenderingFileResourceService.getRenderingFileResources(digitalObjectUuid);
  }

  @Override
  public List<FileResource> getRenderingFileResources(DigitalObject digitalObject)
      throws ServiceException {
    return digitalObjectRenderingFileResourceService.getRenderingFileResources(digitalObject);
  }

  @Override
  public void save(DigitalObject digitalObject) throws ServiceException, ValidationException {
    // Keep the resources for later saving, because the repository save
    // method returns the DigitalObject with empty fields there!
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();

    super.save(digitalObject);

    // save the linked data resources
    setLinkedDataFileResources(digitalObject, linkedDataResources);

    // save the rendering resources
    try {
      setRenderingFileResources(digitalObject, renderingResources);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot update DigitalObject: " + e, e);
    }
    fillDigitalObject(digitalObject);
  }

  @Override
  public List<FileResource> setFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository)
          .setFileResources(digitalObjectUuid, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot set file resources", e);
    }
  }

  @Override
  public List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException {
    return digitalObjectLinkedDataFileResourceService.setLinkedDataFileResources(
        digitalObjectUuid, linkedDataFileResources);
  }

  @Override
  public List<FileResource> setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingFileResources) throws ServiceException {
    return digitalObjectRenderingFileResourceService.setRenderingFileResources(
        digitalObjectUuid, renderingFileResources);
  }

  @Override
  public List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws ServiceException {
    return digitalObjectRenderingFileResourceService.setRenderingFileResources(
        digitalObject, renderingFileResources);
  }

  @Override
  public void update(DigitalObject digitalObject) throws ValidationException, ServiceException {
    // Keep the resources for later saving, because the repository save
    // method returns the DigitalObject with empty fields there!
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();

    super.update(digitalObject);

    // save the linked data resources
    setLinkedDataFileResources(digitalObject, linkedDataResources);

    // save the rendering resources
    try {
      setRenderingFileResources(digitalObject, renderingResources);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot update DigitalObject: " + e, e);
    }
    fillDigitalObject(digitalObject);
  }
}
