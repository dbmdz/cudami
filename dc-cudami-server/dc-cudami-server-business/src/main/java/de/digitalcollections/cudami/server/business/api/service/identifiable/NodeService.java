package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public interface NodeService<N extends Identifiable> extends IdentifiableService<N> {

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
            .filter(c -> c.getUuid() != null)
            .map(c -> c.getUuid())
            .collect(Collectors.toList());
    return addChildren(parent.getUuid(), childrenUuids);
  }

  boolean addChildren(UUID parentUuid, List<UUID> childrenUUIDs);

  /**
   * Build and return the breadcrumb navigation for the given node UUID
   *
   * @param nodeUuid the uuid of the node.
   * @return BreadcrumbNavigation with labels in all available languages
   */
  BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid);

  /**
   * Build and return the breadcrumb navigation for the given webpage UUID and desired locale. If no
   * label for that locale exists, use the label for the fallbackLocale, and if even this fails, use
   * the first locale.
   *
   * @param nodeUuid the uuid of the webpage
   * @param locale the desired locale for the navigation item labels
   * @param fallbackLocale the fallback locale for the navigation item labels
   * @return Breadcrumb navigation with labels in the desired language (if possible)
   */
  default BreadcrumbNavigation getBreadcrumbNavigation(
      UUID nodeUuid, Locale locale, Locale fallbackLocale) {

    BreadcrumbNavigation localizedBreadcrumbNavigation = getBreadcrumbNavigation(nodeUuid);

    localizedBreadcrumbNavigation.getNavigationItems().stream()
        .forEach(
            n -> {
              cleanupLabelFromUnwantedLocales(locale, fallbackLocale, n.getLabel());
            });

    return localizedBreadcrumbNavigation;
  }

  default List<N> getChildren(N node) {
    if (node == null) {
      return null;
    }
    return getChildren(node.getUuid());
  }

  List<N> getChildren(UUID nodeUuid);

  PageResponse<N> findChildren(UUID uuid, PageRequest pageRequest);

  default N getParent(N node) {
    if (node == null) {
      return null;
    }
    return getParent(node.getUuid());
  }

  N getParent(UUID nodeUuid);

  List<N> getParents(UUID uuid);

  PageResponse<N> findRootNodes(PageRequest pageRequest);

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
   * @param parentUuid parent node the new node is child of
   * @return saved child node
   * @throws ServiceException if saving fails
   */
  default N saveWithParent(N child, UUID parentUuid) throws ServiceException, ValidationException {
    if (child.getUuid() == null) {
      save(child);
    }
    return saveWithParent(child.getUuid(), parentUuid);
  }

  /**
   * @param childUuid UUID of newly created child node
   * @param parentUuid parent node the new node is child of
   * @return saved child node
   * @throws ServiceException if saving fails
   */
  N saveWithParent(UUID childUuid, UUID parentUuid) throws ServiceException;

  boolean updateChildrenOrder(UUID parentUuid, List<N> children);
}
