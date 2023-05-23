package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
  private final CorporateBodyService corporateBodyService;
  private final DigitalObjectLinkedDataFileResourceService
      digitalObjectLinkedDataFileResourceService;
  private final DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService;
  private final ItemService itemService;
  private final ManifestationService manifestationService;
  private final PersonService personService;
  private final ProjectService projectService;
  private final WorkService workService;

  public DigitalObjectServiceImpl(
      DigitalObjectRepository repository,
      CorporateBodyService corporateBodyService,
      CollectionService collectionService,
      ProjectService projectService,
      IdentifierService identifierService,
      ItemService itemService,
      ManifestationService manifestationService,
      PersonService personService,
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
    this.corporateBodyService = corporateBodyService;
    this.collectionService = collectionService;
    this.itemService = itemService;
    this.projectService = projectService;
    this.manifestationService = manifestationService;
    this.personService = personService;
    this.workService = workService;
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
  public DigitalObject getByExample(DigitalObject example) throws ServiceException {
    DigitalObject digitalObject = super.getByExample(example);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByExampleAndLocale(DigitalObject example, Locale locale)
      throws ServiceException {
    DigitalObject digitalObject = super.getByExampleAndLocale(example, locale);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  public DigitalObject getByExampleWithWEMI(DigitalObject example) throws ServiceException {
    DigitalObject digitalObject = getByExample(example);
    if (digitalObject == null) {
      return null;
    }

    fillWMID(digitalObject);
    return digitalObject;
  }

  private void fillWMID(DigitalObject digitalObject) throws ServiceException {
    if (digitalObject.getItem() != null) {
      Item item = itemService.getByExample(digitalObject.getItem());
      if (item.getManifestation() != null) {
        item.setManifestation(fillManifestation(item.getManifestation()));
      }
      if (item.getPartOfItem() != null) {
        item.setPartOfItem(itemService.getByExample(item.getPartOfItem()));
      }

      digitalObject.setItem(item);
    }
  }

  @Override
  public DigitalObject getByIdentifier(Identifier identifier) throws ServiceException {
    DigitalObject digitalObject = super.getByIdentifier(identifier);
    fillDigitalObject(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByIdentifierWithWEMI(Identifier identifier) throws ServiceException {
    DigitalObject digitalObject = getByIdentifier(identifier);
    if (digitalObject == null) {
      return null;
    }

    fillWMID(digitalObject);
    return digitalObject;
  }

  @Override
  public DigitalObject getByRefId(long refId) throws ServiceException {
    DigitalObject digitalObject = super.getByRefId(refId);
    fillDigitalObject(digitalObject);
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
    fillDigitalObject(digitalObject);
  }

  @Override
  public List<FileResource> setFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) throws ServiceException {
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
  public List<LinkedDataFileResource> setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException {
    return digitalObjectLinkedDataFileResourceService.setLinkedDataFileResources(
        digitalObject, linkedDataFileResources);
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

  private Manifestation fillManifestation(Manifestation manifestation) throws ServiceException {
    manifestation = manifestationService.getByExample(manifestation);
    if (manifestation.getWork() != null) {
      manifestation.setWork(fillWork(manifestation.getWork()));
    }
    List<RelationSpecification<Manifestation>> parentManifestations = manifestation.getParents();
    if (parentManifestations != null && !parentManifestations.isEmpty()) {
      manifestation.setParents(
          manifestation.getParents().stream()
              .map(
                  r -> {
                    try {
                      r.setSubject(manifestationService.getByExample(r.getSubject()));
                      return r;
                    } catch (ServiceException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .toList());
    }
    if (manifestation.getRelations() != null && manifestation.getRelations().isEmpty()) {
      manifestation.setRelations(fillRelations(manifestation.getRelations()));
    }
    return manifestation;
  }

  private Work fillWork(Work work) throws ServiceException {
    work = workService.getByExample(work);
    if (work.getParents() != null && !work.getParents().isEmpty()) {
      work.setParents(
          work.getParents().stream()
              .map(
                  p -> {
                    try {
                      return workService.getByExample(p);
                    } catch (ServiceException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .toList());
      if (work.getRelations() != null && !work.getRelations().isEmpty()) {
        work.setRelations(fillRelations(work.getRelations()));
      }
    }
    return work;
  }

  private List<EntityRelation> fillRelations(List<EntityRelation> relations) {
    return relations.stream()
        .map(
            r -> {
              try {
                Entity e = r.getSubject();
                switch (e.getIdentifiableObjectType()) {
                  case CORPORATE_BODY -> r.setSubject(
                      corporateBodyService.getByExample(
                          CorporateBody.builder().uuid(e.getUuid()).build()));
                  case PERSON -> r.setSubject(
                      personService.getByExample(Person.builder().uuid(e.getUuid()).build()));
                }
                return r;
              } catch (ServiceException e) {
                throw new RuntimeException(e);
              }
            })
        .toList();
  }
}
