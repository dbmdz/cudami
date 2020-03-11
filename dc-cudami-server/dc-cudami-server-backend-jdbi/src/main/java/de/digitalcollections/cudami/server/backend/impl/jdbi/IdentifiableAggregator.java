package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IdentifiableAggregator<I extends Identifiable> {

  public I identifiable;
  public Set<UUID> identifiers = new HashSet<>();

  public IdentifiableAggregator() {}

  public IdentifiableAggregator(I identifiable) {
    this.identifiable = identifiable;
  }
}
