package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;

/** Service for Topic. */
public interface TopicService extends NodeService<Topic>, EntityService<Topic> {

  PageResponse<Entity> findEntities(Topic topic, PageRequest pageRequest) throws ServiceException;

  PageResponse<FileResource> findFileResources(Topic topic, PageRequest pageRequest)
      throws ServiceException;

  List<FileResource> getFileResources(Topic topic) throws ServiceException;

  List<Locale> getLanguagesOfEntities(Topic topic) throws ServiceException;

  List<Locale> getLanguagesOfFileResources(Topic topic) throws ServiceException;

  // TODO: move to entityservice?
  List<Topic> getTopicsOfEntity(Entity entity);

  // TODO: move to fileresourceservice?
  List<Topic> getTopicsOfFileResource(FileResource fileResource) throws ServiceException;

  List<Entity> setEntities(Topic topic, List<Entity> entities) throws ServiceException;

  List<FileResource> setFileResources(Topic topic, List<FileResource> fileResources)
      throws ServiceException;
}
