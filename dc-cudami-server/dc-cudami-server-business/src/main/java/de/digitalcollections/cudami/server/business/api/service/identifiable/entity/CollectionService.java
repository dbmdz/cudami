package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface CollectionService extends NodeService<Collection>, EntityService<Collection> {

  PageResponse<Collection> getTopCollections(PageRequest pageRequest);

  Collection saveWithParentCollection(Collection collection, UUID parentUuid)
      throws IdentifiableServiceException;

  boolean addDigitalObject(Collection collection, DigitalObject digitalObject);

  boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects);

  boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects);

  PageResponse<DigitalObject> getDigitalObjects(Collection collection, PageRequest pageRequest);
}
