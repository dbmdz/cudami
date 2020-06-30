package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.List;
import java.util.UUID;

public interface NodeRepository<N extends Node> extends IdentifiableRepository<N> {

  N getParent(UUID nodeUuid);

  List<N> getChildren(N node);

  List<N> getChildren(UUID nodeUuid);

  BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid);
}
