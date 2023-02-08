package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface WorkService extends EntityService<Work> {

  Work getForItem(UUID itemUuid);

  Set<Work> getForPerson(UUID personUuid);
}
