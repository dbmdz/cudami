package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

public interface HeadwordEntryService extends EntityService<HeadwordEntry> {

  public List<HeadwordEntry> findByHeadword(UUID uuid);

  List<Agent> getCreators(UUID headwordEntryUuid);
}
