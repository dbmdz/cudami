package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.List;
import java.util.UUID;

public interface NodeRepository<N extends Node> extends IdentifiableRepository<N> {

  N getParent(UUID nodeUuid);

  List<N> getChildren(N node);

  List<N> getChildren(UUID nodeUuid);

  PageResponse<N> getChildren(UUID nodeUuid, PageRequest pageRequest);

  /**
   * @param nodeUuid the uuid of the current node
   * @return the breadcrumb navigation
   */
  BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid);
}
