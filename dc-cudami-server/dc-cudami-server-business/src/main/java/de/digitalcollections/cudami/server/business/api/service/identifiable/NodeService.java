package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;

public interface NodeService<N extends Identifiable> extends IdentifiableService<N> {

  boolean addChild(N parent, N child);

  boolean addChildren(N parent, List<N> children);

  PageResponse<N> findChildren(N parent, PageRequest pageRequest);

  PageResponse<N> findRootNodes(PageRequest pageRequest);

  /**
   * Build and return the breadcrumb navigation for the given node and desired locale. If no label
   * for that locale exists, use the label for the fallbackLocale, and if even this fails, use the
   * first locale.
   *
   * @param nodeUuid the uuid of the webpage
   * @param locale the desired locale for the navigation item labels
   * @param fallbackLocale the fallback locale for the navigation item labels
   * @return Breadcrumb navigation with labels in the desired language (if possible)
   */
  default BreadcrumbNavigation getBreadcrumbNavigation(
      N node, Locale locale, Locale fallbackLocale) {
    BreadcrumbNavigation localizedBreadcrumbNavigation = getBreadcrumbNavigation(node);

    localizedBreadcrumbNavigation.getNavigationItems().stream()
        .forEach(
            n -> {
              cleanupLabelFromUnwantedLocales(locale, fallbackLocale, n.getLabel());
            });

    return localizedBreadcrumbNavigation;
  }

  /**
   * Build and return the breadcrumb navigation for the given node UUID
   *
   * @param nodeUuid the uuid of the node.
   * @return BreadcrumbNavigation with labels in all available languages
   */
  BreadcrumbNavigation getBreadcrumbNavigation(N node);

  List<N> getChildren(N node);

  N getParent(N node);

  List<N> getParents(N node);

  List<Locale> getRootNodesLanguages();

  boolean removeChild(N parent, N child);

  /**
   * @param child newly created child node to be saved
   * @param parentUuid parent node the new node is child of
   * @return saved child node
   * @throws ServiceException if saving fails
   */
  N saveWithParent(N child, N parent);

  boolean updateChildrenOrder(N parent, List<N> children);
}
