package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for Topic handling. */
@Service
@Transactional(rollbackFor = {IdentifiableServiceException.class, RuntimeException.class})
public class TopicServiceImpl extends EntityServiceImpl<Topic> implements TopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicServiceImpl.class);

  @Autowired
  public TopicServiceImpl(
      TopicRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    return ((NodeRepository<Topic>) repository).addChildren(parentUuid, childrenUuids);
  }

  @Override
  public SearchPageResponse<Topic> findChildren(UUID uuid, SearchPageRequest searchPageRequest) {
    return ((NodeRepository<Topic>) repository).findChildren(uuid, searchPageRequest);
  }

  @Override
  public List<Entity> getAllEntities(UUID topicUuid) {
    return ((TopicRepository) repository).getAllEntities(topicUuid);
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
  public PageResponse<Topic> getChildren(UUID nodeUuid, PageRequest pageRequest) {
    return ((NodeRepository<Topic>) repository).getChildren(nodeUuid, pageRequest);
  }

  @Override
  public PageResponse<Entity> getEntities(UUID topicUuid, PageRequest pageRequest) {
    return ((TopicRepository) repository).getEntities(topicUuid, pageRequest);
  }

  @Override
  public List<FileResource> getFileResources(UUID topicUuid) {
    return ((TopicRepository) repository).getFileResources(topicUuid);
  }

  @Override
  public PageResponse<FileResource> getFileResources(UUID topicUuid, PageRequest pageRequest) {
    return ((TopicRepository) repository).getFileResources(topicUuid, pageRequest);
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
  public PageResponse<Topic> getRootNodes(PageRequest pageRequest) {
    return ((NodeRepository<Topic>) repository).getRootNodes(pageRequest);
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
  public List<Entity> saveEntities(UUID topicUuid, List<Entity> entities) {
    return ((TopicRepository) repository).saveEntities(topicUuid, entities);
  }

  @Override
  public List<FileResource> saveFileResources(UUID topicUuid, List<FileResource> fileResources) {
    return ((TopicRepository) repository).saveFileResources(topicUuid, fileResources);
  }

  @Override
  public Topic saveWithParent(UUID childUuid, UUID parentUuid) throws IdentifiableServiceException {
    return ((NodeRepository<Topic>) repository).saveWithParent(childUuid, parentUuid);
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Topic> children) {
    return ((NodeRepository<Topic>) repository).updateChildrenOrder(parentUuid, children);
  }

  @Override
  public SearchPageResponse<Topic> findRootNodes(SearchPageRequest searchPageRequest) {
    setDefaultSorting(searchPageRequest);
    return ((NodeRepository<Topic>) repository).findRootNodes(searchPageRequest);
  }
}
