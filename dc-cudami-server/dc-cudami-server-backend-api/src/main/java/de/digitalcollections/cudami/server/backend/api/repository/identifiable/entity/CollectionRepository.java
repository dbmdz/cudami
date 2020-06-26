package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Repository for Collection persistence handling. */
public interface CollectionRepository
    extends NodeRepository<Collection>, EntityRepository<Collection> {

  @Override
  default List<Collection> getChildren(Collection collection) {
    if (collection == null) {
      return null;
    }
    return getChildren(collection.getUuid());
  }

  Collection saveWithParentCollection(Collection collection, UUID parentUuid);

  PageResponse<Collection> getRootCollections(PageRequest pageRequest);
}
