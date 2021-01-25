package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface NodeService<N extends Node> extends IdentifiableService<N> {

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

  boolean addChildren(UUID parentUuid, List<N> collections);

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
              cleanupLabelFromUnwantedLocales(locale, fallbackLocale, n);
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

  PageResponse<N> getChildren(UUID uuid, PageRequest pageRequest);

  default N getParent(N node) {
    if (node == null) {
      return null;
    }
    return getParent(node.getUuid());
  }

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
   * @param parentUuid parent node the new node is child of
   * @return saved child node
   * @throws IdentifiableServiceException if saving fails
   */
  N saveWithParent(N child, UUID parentUuid) throws IdentifiableServiceException;

  boolean updateChildrenOrder(UUID parentUuid, List<N> children);
}
