package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.UUID;

/** Repository for HeadwordEntry persistence handling. */
public interface HeadwordEntryRepository extends EntityRepository<HeadwordEntry> {

  // TODO: replace with find(pagerequest)
  List<HeadwordEntry> getByHeadword(UUID headwordUuid) throws RepositoryException;

  // TODO: replace with find(pagerequest)
  List<Agent> getCreators(UUID headwordEntryUuid) throws RepositoryException;
}
