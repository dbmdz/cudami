package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends EntityServiceImpl<Collection>
    implements CollectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionServiceImpl.class);

  @Autowired
  public CollectionServiceImpl(CollectionRepository repository) {
    super(repository);
  }

  @Override
  public Collection saveWithParentCollection(Collection collection, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((CollectionRepository) repository).saveWithParentCollection(collection, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + collection + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public Collection getParent(Collection node) {
    return getParent(node.getUuid());
  }

  @Override
  public Collection getParent(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getParent(nodeUuid);
  }

  @Override
  public List<Collection> getChildren(Collection collection) {
    return ((NodeRepository<Collection>) repository).getChildren(collection);
  }

  @Override
  public List<Collection> getChildren(UUID uuid) {
    return ((NodeRepository<Collection>) repository).getChildren(uuid);
  }

  @Override
  public PageResponse<Collection> getChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository<Collection>) repository).getChildren(uuid, pageRequest);
  }

  @Override
  public PageResponse<Collection> getTopCollections(PageRequest pageRequest) {
    return ((CollectionRepository) repository).getTopCollections(pageRequest);
  }
}
