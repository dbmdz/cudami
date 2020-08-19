package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface ItemRepository extends IdentifiableRepository<Item> {

  PageResponse<Item> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  Set<DigitalObject> getDigitalObjects(UUID itemUuid);

  Set<Work> getWorks(UUID itemUuid);

  boolean addWork(UUID itemUuid, UUID workUuid);

  boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid);
}
