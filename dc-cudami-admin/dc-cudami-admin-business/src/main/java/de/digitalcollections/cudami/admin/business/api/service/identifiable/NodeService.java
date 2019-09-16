package de.digitalcollections.cudami.admin.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Node;
import java.util.List;
import java.util.UUID;

public interface NodeService<N extends Node> extends IdentifiableService<N> {

  N getParent(N node);

  N getParent(UUID nodeUuid);

  List<N> getChildren(N node);

  List<N> getChildren(UUID uuid);
}
