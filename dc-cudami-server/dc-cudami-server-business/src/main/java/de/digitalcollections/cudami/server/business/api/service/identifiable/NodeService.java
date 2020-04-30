package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface NodeService<N extends Node> extends IdentifiableService<N> {

  N getParent(N node);

  N getParent(UUID nodeUuid);

  List<N> getChildren(N node);

  List<N> getChildren(UUID uuid);

  PageResponse<N> getChildren(UUID uuid, PageRequest pageRequest);
}
