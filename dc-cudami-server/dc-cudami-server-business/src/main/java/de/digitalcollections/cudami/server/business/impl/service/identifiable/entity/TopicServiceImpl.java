package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for Topic handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class TopicServiceImpl extends EntityServiceImpl<Topic> implements TopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicServiceImpl.class);

  public TopicServiceImpl(
      TopicRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
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
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    return ((NodeRepository<Topic>) repository).addChildren(parentUuid, childrenUuids);
  }

  @Override
  public PageResponse<Topic> findChildren(UUID nodeUuid, PageRequest pageRequest) {
    return ((NodeRepository<Topic>) repository).findChildren(nodeUuid, pageRequest);
  }

  @Override
  public PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest) {
    return ((TopicRepository) repository).findEntities(topicUuid, pageRequest);
  }

  @Override
  public PageResponse<FileResource> findFileResources(UUID topicUuid, PageRequest pageRequest) {
    return ((TopicRepository) repository).findFileResources(topicUuid, pageRequest);
  }

  @Override
  public PageResponse<Topic> findRootNodes(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return ((NodeRepository<Topic>) repository).findRootNodes(pageRequest);
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((NodeRepository<Topic>) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public List<Topic> getChildren(UUID nodeUuid) {
    return ((NodeRepository<Topic>) repository).getChildren(nodeUuid);
  }

  @Override
  public List<Entity> getEntities(UUID topicUuid) {
    return ((TopicRepository) repository).getEntities(topicUuid);
  }

  @Override
  public List<FileResource> getFileResources(UUID topicUuid) {
    return ((TopicRepository) repository).getFileResources(topicUuid);
  }

  @Override
  public List<Locale> getLanguagesOfEntities(UUID topicUuid) {
    return ((TopicRepository) repository).getLanguagesOfEntities(topicUuid);
  }

  @Override
  public List<Locale> getLanguagesOfFileResources(UUID topicUuid) {
    return ((TopicRepository) repository).getLanguagesOfFileResources(topicUuid);
  }

  @Override
  public Topic getParent(UUID nodeUuid) {
    return ((NodeRepository<Topic>) repository).getParent(nodeUuid);
  }

  @Override
  public List<Topic> getParents(UUID nodeUuid) {
    return ((NodeRepository<Topic>) repository).getParents(nodeUuid);
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    return ((NodeRepository<Topic>) repository).getRootNodesLanguages();
  }

  @Override
  public List<Topic> getTopicsOfEntity(UUID entityUuid) {
    return ((TopicRepository) repository).getTopicsOfEntity(entityUuid);
  }

  @Override
  public List<Topic> getTopicsOfFileResource(UUID fileResourceUuid) {
    return ((TopicRepository) repository).getTopicsOfFileResource(fileResourceUuid);
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    return ((NodeRepository<Topic>) repository).removeChild(parentUuid, childUuid);
  }

  @Override
  public Topic saveWithParent(UUID childUuid, UUID parentUuid) throws IdentifiableServiceException {
    return ((NodeRepository<Topic>) repository).saveWithParent(childUuid, parentUuid);
  }

  @Override
  public List<Entity> setEntities(UUID topicUuid, List<Entity> entities) {
    return ((TopicRepository) repository).setEntities(topicUuid, entities);
  }

  @Override
  public List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources) {
    return ((TopicRepository) repository).setFileResources(topicUuid, fileResources);
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Topic> children) {
    return ((NodeRepository<Topic>) repository).updateChildrenOrder(parentUuid, children);
  }
}
