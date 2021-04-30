package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public interface NodeRepository<N extends Identifiable> extends IdentifiableRepository<N> {

  default boolean addChild(N parent, N child) {
    if (parent == null || child == null) {
      return false;
    }
    return addChild(parent.getUuid(), child.getUuid());
  }

  default boolean addChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    return addChildren(parentUuid, Arrays.asList(childUuid));
  }

  default boolean addChildren(N parent, List<N> children) {
    if (parent == null || children == null) {
      return false;
    }
    List<UUID> childrenUuids =
        children.stream()
            .filter(c -> c.getUuid() == null)
            .map(c -> c.getUuid())
            .collect(Collectors.toList());
    return addChildren(parent.getUuid(), childrenUuids);
  }

  boolean addChildren(UUID parentUuid, List<UUID> childrenUUIDs);

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

  SearchPageResponse<N> findChildren(UUID uuid, SearchPageRequest searchPageRequest);

  N getParent(UUID nodeUuid);

  List<N> getParents(UUID uuid);

  PageResponse<N> getRootNodes(PageRequest pageRequest);

  SearchPageResponse<N> findRootNodes(SearchPageRequest searchPageRequest);

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
