package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface NodeRepository<N extends Node> extends IdentifiableRepository<N> {

  default boolean addChild(N parent, N child) {
    if (parent == null || child == null) {
      return false;
    }
    return addChildren(parent.getUuid(), Arrays.asList(child));
  }

  default boolean addChildren(N parent, List<N> children) {
    if (parent == null || children == null) {
      return false;
    }
    return addChildren(parent.getUuid(), children);
  }

  boolean addChildren(UUID parentUuid, List<N> children);

  /**
   * @param nodeUuid the uuid of the current node
   * @return the breadcrumb navigation
   */
  BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid);

  default List<N> getChildren(N node) {
    if (node == null) {
      return null;
    }
    return getChildren(node.getUuid());
  }

  List<N> getChildren(UUID nodeUuid);

  PageResponse<N> getChildren(UUID nodeUuid, PageRequest pageRequest);

  N getParent(UUID nodeUuid);

  List<N> getParents(UUID uuid);

  PageResponse<N> getRootNodes(PageRequest pageRequest);

  List<Locale> getRootNodesLanguages();

  default boolean removeChild(N parent, N child) {
    if (parent == null || child == null) {
      return false;
    }
    return removeChild(parent.getUuid(), child.getUuid());
  }

  boolean removeChild(UUID parentUuid, UUID childUuid);

  /**
   * @param child newly created child node to be saved
   * @param parentUUID parent node the new node is child of
   * @return saved child node
   */
  N saveWithParent(N child, UUID parentUUID);

  boolean updateChildrenOrder(UUID parentUuid, List<N> children);
}
