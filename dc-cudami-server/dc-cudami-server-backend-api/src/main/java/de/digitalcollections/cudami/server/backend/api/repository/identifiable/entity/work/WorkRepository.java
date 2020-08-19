package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface WorkRepository extends IdentifiableRepository<Work> {

  PageResponse<Work> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  List<Agent> getCreators(UUID workUuid);

  List<Item> getItems(UUID workUuid);
}
