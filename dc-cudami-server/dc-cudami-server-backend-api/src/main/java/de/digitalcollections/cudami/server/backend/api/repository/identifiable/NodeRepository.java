package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public interface NodeRepository<N extends Identifiable> extends IdentifiableRepository<N> {

  default boolean addChild(N parent, N child) throws RepositoryException {
    if (parent == null || child == null) {
      throw new IllegalArgumentException("parent and child must not be null");
    }
    return addChild(parent.getUuid(), child.getUuid());
  }

  default boolean addChild(UUID parentUuid, UUID childUuid) throws RepositoryException {
    if (parentUuid == null || childUuid == null) {
      throw new IllegalArgumentException("parent and child uuids must not be null");
    }
    return addChildren(parentUuid, Arrays.asList(childUuid));
  }

  default boolean addChildren(N parent, List<N> children) throws RepositoryException {
    if (parent == null || children == null) {
      throw new IllegalArgumentException("parent and children must not be null");
    }
    List<UUID> childrenUuids =
        children.stream()
            .filter(c -> c.getUuid() != null)
            .map(c -> c.getUuid())
            .collect(Collectors.toList());
    return addChildren(parent.getUuid(), childrenUuids);
  }

  boolean addChildren(UUID parentUuid, List<UUID> childrenUUIDs) throws RepositoryException;

  default PageResponse<N> findChildren(N parent, PageRequest pageRequest)
      throws RepositoryException {
    if (parent == null) {
      throw new IllegalArgumentException("parent must not be null");
    }
    return findChildren(parent.getUuid(), pageRequest);
  }

  PageResponse<N> findChildren(UUID nodeUuid, PageRequest pageRequest) throws RepositoryException;

  PageResponse<N> findRootNodes(PageRequest pageRequest) throws RepositoryException;

  default BreadcrumbNavigation getBreadcrumbNavigation(N node) throws RepositoryException {
    if (node == null) {
      throw new IllegalArgumentException("node must not be null");
    }
    return getBreadcrumbNavigation(node.getUuid());
  }

  /**
   * @param nodeUuid the uuid of the current node
   * @return the breadcrumb navigation
   */
  BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) throws RepositoryException;

  default List<N> getChildren(N node) throws RepositoryException {
    if (node == null) {
      throw new IllegalArgumentException("node must not be null");
    }
    return getChildren(node.getUuid());
  }

  List<N> getChildren(UUID nodeUuid) throws RepositoryException;

  default N getParent(N node) throws RepositoryException {
    if (node == null) {
      throw new IllegalArgumentException("node must not be null");
    }
    return getParent(node.getUuid());
  }

  N getParent(UUID nodeUuid) throws RepositoryException;

  default List<N> getParents(N node) throws RepositoryException {
    if (node == null) {
      throw new IllegalArgumentException("node must not be null");
    }
    return getParents(node.getUuid());
  }

  List<N> getParents(UUID uuid) throws RepositoryException;

  List<Locale> getRootNodesLanguages() throws RepositoryException;

  default boolean removeChild(N parent, N child) throws RepositoryException {
    if (parent == null || child == null) {
      throw new IllegalArgumentException("parent and child must not be null");
    }
    return removeChild(parent.getUuid(), child.getUuid());
  }

  boolean removeChild(UUID parentUuid, UUID childUuid) throws RepositoryException;

  default N saveParentRelation(N child, N parent) throws RepositoryException {
    if (parent == null || child == null) {
      throw new IllegalArgumentException("Parent and child must not be null");
    }
    if (parent.getUuid() == null || child.getUuid() == null) {
      throw new IllegalArgumentException("Parent and Child must have been saved already");
    }
    return saveParentRelation(child.getUuid(), parent.getUuid());
  }

  /**
   * @param childUuid UUID of (newly created) child node
   * @param parentUuid parent node the new node is child of
   * @return saved child node
   * @throws RepositoryException if saving fails
   */
  N saveParentRelation(UUID childUuid, UUID parentUuid) throws RepositoryException;

  default boolean updateChildrenOrder(N parent, List<N> children) throws RepositoryException {
    if (parent == null || children == null) {
      throw new IllegalArgumentException("parent and children must not be null");
    }
    List<UUID> childrenUuids =
        children.stream()
            .filter(Objects::nonNull)
            .map(Identifiable::getUuid)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return updateChildrenOrder(parent.getUuid(), childrenUuids);
  }

  boolean updateChildrenOrder(UUID parentUuid, List<UUID> children) throws RepositoryException;
}
