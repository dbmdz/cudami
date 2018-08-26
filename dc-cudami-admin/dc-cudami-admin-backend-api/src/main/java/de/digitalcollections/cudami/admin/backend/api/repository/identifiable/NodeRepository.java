package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Node;
import java.util.List;
import java.util.UUID;

public interface NodeRepository<N extends Node> extends IdentifiableRepository<N> {
  List<N> getChildren(N node);
  
  List<N> getChildren(UUID uuid);

  void addContent(N node, Identifiable identifiable);
}
