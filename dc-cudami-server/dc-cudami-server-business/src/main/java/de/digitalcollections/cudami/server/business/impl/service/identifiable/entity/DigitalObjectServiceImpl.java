package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
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
  private final ProjectService projectService;

  private final ItemService itemService;
  private final ManifestationService manifestationService;
  private final WorkService workService;

  public DigitalObjectServiceImpl(
      DigitalObjectRepository repository,
      CollectionService collectionService,
      ProjectService projectService,
      IdentifierService identifierService,
      ItemService itemService,
      ManifestationService manifestationService,
      WorkService workService,
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
    this.manifestationService = manifestationService;
    this.workService = workService;
    this.projectService = projectService;
    this.digitalObjectRenderingFileResourceService = digitalObjectRenderingFileResourceService;
    this.digitalObjectLinkedDataFileResourceService = digitalObjectLinkedDataFileResourceService;
  }

  @Override
  public boolean delete(DigitalObject digitalObject) throws ServiceException, ConflictException {
    // Check for existance. If not given, return false.
    DigitalObject digitalObjectFromRepo = getByExample(digitalObject);
    if (digitalObjectFromRepo == null) {
      return false;
    }

    // Remove connection to collections
    collectionService.removeDigitalObjectFromAllCollections(digitalObjectFromRepo);

    // Remove connection to projects
    projectService.removeDigitalObjectFromAllProjects(digitalObjectFromRepo);

    // Remove preview images
    deleteFileResources(digitalObjectFromRepo);

    // Remove LinkedDataFileResources (relation, and, if possible, resource)
    try {
      deleteLinkedDatafileResources(digitalObjectFromRepo);
    } catch (ServiceException e) {
      throw new ServiceException(
          "Cannot remove LinkedDataFileResource from digitalObject="
              + digitalObjectFromRepo
              + ": "
              + e,
          e);
    }

    // Remove RenderingResources (relation, and, if possible, resource)
    try {
      deleteRenderingFileResourceResource(digitalObjectFromRepo);
    } catch (ServiceException e) {
      throw new ServiceException(
          "Cannot remove RenderingFileResource from digitalObject="
              + digitalObjectFromRepo
              + ": "
              + e,
          e);
    }

    return super.delete(digitalObjectFromRepo);
  }

  @Override
  public void deleteFileResources(DigitalObject digitalObject) throws ServiceException {
    try {
      ((DigitalObjectRepository) repository).deleteFileResources(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  private void deleteLinkedDatafileResources(DigitalObject digitalObject) throws ServiceException {
    digitalObjectLinkedDataFileResourceService.deleteLinkedDataFileResources(digitalObject);
  }

  private void deleteRenderingFileResourceResource(DigitalObject digitalObject)
      throws ServiceException {
    digitalObjectRenderingFileResourceService.deleteRenderingFileResources(digitalObject);
  }

  private void fillDigitalObject(DigitalObject digitalObject) throws ServiceException {
    if (digitalObject == null) {
      return;
    }

    // Look for linked data file resources. If they exist, fill the DigitalObject
    List<LinkedDataFileResource> linkedDataFileResources =
        getLinkedDataFileResources(digitalObject);
    if (linkedDataFileResources != null && !linkedDataFileResources.isEmpty()) {
      digitalObject.setLinkedDataResources(new ArrayList<>(linkedDataFileResources));
    }

    // Look for rendering resources. If they exist, fill the object
    List<FileResource> renderingResources = getRenderingFileResources(digitalObject);
    if (renderingResources != null && !renderingResources.isEmpty()) {
      digitalObject.setRenderingResources(new ArrayList<>(renderingResources));
    }
  }

  @Override
  public PageResponse<Collection> findActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    pageRequest.add(filtering);
    try {
      return ((DigitalObjectRepository) repository).findCollections(digitalObject, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Collection> findCollections(
      DigitalObject digitalObject, PageRequest pageRequest) throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).findCollections(digitalObject, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Project> findProjects(DigitalObject digitalObject, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).findProjects(digitalObject, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjectsByItem(Item item, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).findDigitalObjectsByItem(item, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  private void expandByWemiObjects(DigitalObject digitalObject) throws ServiceException {
    if (digitalObject == null || digitalObject.getItem() == null) return;
    // local function to set lastModified of the DigitalObject to the newest of its enclosed WMI
    // objects
    BiConsumer<DigitalObject, UniqueObject> setNewestLastModified =
        (digObj, wmiObject) -> {
          if (wmiObject.getLastModified().isAfter(digObj.getLastModified()))
            digObj.setLastModified(wmiObject.getLastModified());
          wmiObject.setLastModified(null);
        };

    Item item = itemService.getByExample(digitalObject.getItem());
    digitalObject.setItem(item);
    setNewestLastModified.accept(digitalObject, item);

    if (item.getManifestation() == null) return;
    Manifestation manifestation = manifestationService.getByExample(item.getManifestation());
    item.setManifestation(manifestation);
    setNewestLastModified.accept(digitalObject, manifestation);

    if (manifestation.getWork() == null) return;
    Work work = workService.getByExample(manifestation.getWork());
    manifestation.setWork(work);
    setNewestLastModified.accept(digitalObject, work);
  }

  @Override
  public DigitalObject getByExample(DigitalObject example, boolean fillWemi)
      throws ServiceException {
    DigitalObject digitalObject = super.getByExample(example);
    if (fillWemi) expandByWemiObjects(digitalObject);
    return digitalObject;
  }

  @Override
  public List<DigitalObject> getByExamples(List<DigitalObject> examples, boolean fillWemi)
      throws ServiceException {
    List<DigitalObject> digitalObjects = super.getByExamples(examples);
    if (fillWemi) {
      for (DigitalObject digitalObject : digitalObjects) {
        expandByWemiObjects(digitalObject);
      }
    }
    return digitalObjects;
  }

  @Override
  public DigitalObject getByIdentifier(Identifier identifier, boolean fillWemi)
      throws ServiceException {
    DigitalObject digitalObject = super.getByIdentifier(identifier);
    if (fillWemi) expandByWemiObjects(digitalObject);
    return digitalObject;
  }

  @Override
  public List<FileResource> getFileResources(DigitalObject digitalObject) throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).getFileResources(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<ImageFileResource> getIiifImageFileResources(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).getIiifImageFileResources(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<ImageFileResource> getImageFileResources(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).getImageFileResources(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Item getItem(DigitalObject example) throws ServiceException {
    DigitalObject digitalObject = getByExample(example);
    if (digitalObject == null) {
      return null;
    }
    return digitalObject.getItem();
  }

  @Override
  public List<Locale> getLanguagesOfCollections(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).getLanguagesOfCollections(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfContainedDigitalObjects(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository)
          .getLanguagesOfContainedDigitalObjects(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfProjects(DigitalObject digitalObject) throws ServiceException {
    try {
      return ((DigitalObjectRepository) repository).getLanguagesOfProjects(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return digitalObjectLinkedDataFileResourceService.getLinkedDataFileResources(digitalObject);
    } catch (ServiceException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<DigitalObject> getRandom(int count) throws ServiceException {
    List<DigitalObject> digitalObjects = super.getRandom(count);
    if (digitalObjects == null || digitalObjects.isEmpty()) {
      return digitalObjects;
    }
    for (DigitalObject digitalObject : digitalObjects) {
      fillDigitalObject(digitalObject);
    }
    return digitalObjects;
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
      throw new ServiceException("Cannot save DigitalObject: " + e, e);
    }
  }

  @Override
  public List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources)
      throws ServiceException, ValidationException {
    try {
      return ((DigitalObjectRepository) repository).setFileResources(digitalObject, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean setItem(DigitalObject digitalObject, Item item)
      throws ConflictException, ValidationException, ServiceException {
    // If the item does not exist, return false
    if (item == null) {
      return false;
    }

    // Retrieve the DigitalObject
    DigitalObject digitalObjectFromRepo = getByExample(digitalObject);
    if (digitalObjectFromRepo == null) {
      return false;
    }

    // Ensure, that the DigitalObject is either not connected with any item or
    // already belongs to
    // the item
    Item digitalObjectItem = digitalObjectFromRepo.getItem();
    if (digitalObjectItem != null && digitalObjectItem.getUuid().equals(item.getUuid())) {
      return true; // nothing to do
    }
    if (digitalObjectItem != null && !digitalObjectItem.getUuid().equals(item.getUuid())) {
      LOGGER.warn(
          "Trying to connect DigitalObject "
              + digitalObject
              + " to item "
              + item.getUuid()
              + ", but it already belongs to item "
              + digitalObjectItem.getUuid());
      throw new ConflictException(
          "DigitalObject "
              + digitalObjectFromRepo.getUuid()
              + " already belongs to item "
              + digitalObjectFromRepo.getItem().getUuid());
    }

    digitalObjectFromRepo.setItem(item);
    update(digitalObjectFromRepo);
    return true;
  }

  @Override
  public void setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException, ValidationException {
    digitalObjectLinkedDataFileResourceService.setLinkedDataFileResources(
        digitalObject, linkedDataFileResources);
  }

  @Override
  public void setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingFileResources)
      throws ServiceException {
    digitalObjectRenderingFileResourceService.setRenderingFileResources(
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
  }
}
