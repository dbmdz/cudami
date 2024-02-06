package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
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
  public boolean addChild(Topic parent, Topic child) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).addChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addChildren(Topic parent, List<Topic> children) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).addChildren(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addEntities(Topic topic, List<Entity> entities) throws ServiceException {
    try {
      return ((TopicRepository) repository).addEntities(topic, entities);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addEntity(Topic topic, Entity entity) throws ServiceException {
    try {
      return ((TopicRepository) repository).addEntity(topic, entity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addFileResource(Topic topic, FileResource fileResource) throws ServiceException {
    try {
      return ((TopicRepository) repository).addFileResource(topic, fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addFileResources(Topic topic, List<FileResource> fileResources)
      throws ServiceException {
    try {
      return ((TopicRepository) repository).addFileResources(topic, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Topic> findChildren(Topic topic, PageRequest pageRequest)
      throws ServiceException {
    PageResponse<Topic> pageResponse;
    try {
      pageResponse = ((NodeRepository<Topic>) repository).findChildren(topic, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return pageResponse;
  }

  @Override
  public PageResponse<Entity> findEntities(Topic topic, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((TopicRepository) repository).findEntities(topic, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<FileResource> findFileResources(Topic topic, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((TopicRepository) repository).findFileResources(topic, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Topic> findRootNodes(PageRequest pageRequest) throws ServiceException {
    setDefaultSorting(pageRequest);
    PageResponse<Topic> pageResponse;
    try {
      pageResponse = ((NodeRepository<Topic>) repository).findRootNodes(pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return pageResponse;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(Topic topic) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).getBreadcrumbNavigation(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Topic> getChildren(Topic topic) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).getChildren(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<FileResource> getFileResources(Topic topic) throws ServiceException {
    try {
      return ((TopicRepository) repository).getFileResources(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfEntities(Topic topic) throws ServiceException {
    try {
      return ((TopicRepository) repository).getLanguagesOfEntities(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfFileResources(Topic topic) throws ServiceException {
    try {
      return ((TopicRepository) repository).getLanguagesOfFileResources(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Topic getParent(Topic topic) throws ServiceException {
    Topic parent;
    try {
      parent = ((NodeRepository<Topic>) repository).getParent(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return parent;
  }

  @Override
  public List<Topic> getParents(Topic topic) throws ServiceException {
    List<Topic> parents;
    try {
      parents = ((NodeRepository) repository).getParents(topic);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return parents;
  }

  @Override
  public List<Locale> getRootNodesLanguages() throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).getRootNodesLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Topic> getTopicsOfEntity(Entity entity) throws ServiceException {
    try {
      return ((TopicRepository) repository).getTopicsOfEntity(entity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Topic> getTopicsOfFileResource(FileResource fileResource) throws ServiceException {
    try {
      return ((TopicRepository) repository).getTopicsOfFileResource(fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeChild(Topic parent, Topic child) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).removeChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeEntity(Topic topic, Entity entity) throws ServiceException {
    try {
      return ((TopicRepository) repository).removeEntity(topic, entity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeFileResource(Topic topic, FileResource fileResource)
      throws ServiceException {
    try {
      return ((TopicRepository) repository).removeFileResource(topic, fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Topic saveWithParent(Topic child, Topic parent) throws ServiceException {
    try {
      if (child.getUuid() == null) save(child);
      Topic topic = ((TopicRepository) repository).saveParentRelation(child, parent);
      return topic;
    } catch (Exception e) {
      throw new ServiceException("Cannot save topic %s: %s".formatted(child, e.getMessage()), e);
    }
  }

  @Override
  public boolean setEntities(Topic topic, List<Entity> entities) throws ServiceException {
    try {
      return ((TopicRepository) repository).setEntities(topic, entities);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean setFileResources(Topic topic, List<FileResource> fileResources)
      throws ServiceException {
    try {
      return ((TopicRepository) repository).setFileResources(topic, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean updateChildrenOrder(Topic parent, List<Topic> children) throws ServiceException {
    try {
      return ((NodeRepository<Topic>) repository).updateChildrenOrder(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
