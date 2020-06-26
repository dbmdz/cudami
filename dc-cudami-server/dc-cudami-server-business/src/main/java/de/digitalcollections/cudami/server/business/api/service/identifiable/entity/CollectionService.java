package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;

public interface CollectionService extends NodeService<Collection>, EntityService<Collection> {

  PageResponse<Collection> getRootCollections(PageRequest pageRequest);

  Collection saveWithParentCollection(Collection collection, UUID parentUuid)
      throws IdentifiableServiceException;
}
