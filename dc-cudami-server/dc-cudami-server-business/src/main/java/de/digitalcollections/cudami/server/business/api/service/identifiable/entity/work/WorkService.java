package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.List;
import java.util.UUID;

public interface WorkService<W extends Work> extends EntityService<W> {

  List<Agent> getCreators(UUID workUuid);

  List<Item> getItems(UUID uuid);
}
