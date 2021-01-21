package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Work persistence handling.
 *
 * @param <W> instance of work implementation
 */
public interface WorkRepository<W extends Work> extends EntityRepository<W> {

  List<Agent> getCreators(UUID workUuid);

  List<Item> getItems(UUID workUuid);
}
