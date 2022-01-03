package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Repository for HeadwordEntry persistence handling. */
public interface HeadwordEntryRepository extends EntityRepository<HeadwordEntry> {

  List<HeadwordEntry> findByHeadword(UUID headwordUuid);

  List<Agent> getCreators(UUID headwordEntryUuid);
}
