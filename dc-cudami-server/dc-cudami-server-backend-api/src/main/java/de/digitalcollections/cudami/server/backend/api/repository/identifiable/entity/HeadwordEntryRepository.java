package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.UUID;

/** Repository for HeadwordEntry persistence handling. */
public interface HeadwordEntryRepository extends EntityRepository<HeadwordEntry> {

  default List<HeadwordEntry> getByHeadword(Headword headword) throws RepositoryException {
    if (headword == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByHeadword(headword.getUuid());
  }

  // TODO: replace with find(pagerequest)
  List<HeadwordEntry> getByHeadword(UUID headwordUuid) throws RepositoryException;

  default List<Agent> getCreators(HeadwordEntry headwordEntry) throws RepositoryException {
    if (headwordEntry == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getCreators(headwordEntry.getUuid());
  }

  // TODO: replace with find(pagerequest)
  List<Agent> getCreators(UUID headwordEntryUuid) throws RepositoryException;
}
