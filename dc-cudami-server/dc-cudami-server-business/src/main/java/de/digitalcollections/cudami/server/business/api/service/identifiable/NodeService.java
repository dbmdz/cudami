package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;

public interface NodeService<N extends Identifiable> extends IdentifiableService<N> {

  boolean addChild(N parent, N child) throws ServiceException;

  boolean addChildren(N parent, List<N> children) throws ServiceException;

  PageResponse<N> findChildren(N parent, PageRequest pageRequest) throws ServiceException;

  PageResponse<N> findRootNodes(PageRequest pageRequest) throws ServiceException;

  /**
   * Build and return the breadcrumb navigation for the given node and desired locale. If no label
   * for that locale exists, use the label for the fallbackLocale, and if even this fails, use the
   * first locale.
   *
   * @param node the node
   * @param locale the desired locale for the navigation item labels
   * @param fallbackLocale the fallback locale for the navigation item labels
   * @return Breadcrumb navigation with labels in the desired language (if possible)
   */
  default BreadcrumbNavigation getBreadcrumbNavigation(N node, Locale locale, Locale fallbackLocale)
      throws ServiceException {
    BreadcrumbNavigation localizedBreadcrumbNavigation = getBreadcrumbNavigation(node);

    localizedBreadcrumbNavigation.getNavigationItems().stream()
        .forEach(
            n -> {
              cleanupLabelFromUnwantedLocales(locale, fallbackLocale, n.getLabel());
            });

    return localizedBreadcrumbNavigation;
  }

  /**
   * Build and return the breadcrumb navigation for the given node
   *
   * @param node the node.
   * @return BreadcrumbNavigation with labels in all available languages
   */
  BreadcrumbNavigation getBreadcrumbNavigation(N node) throws ServiceException;

  List<N> getChildren(N node) throws ServiceException;

  N getParent(N node) throws ServiceException;

  List<N> getParents(N node) throws ServiceException;

  List<Locale> getRootNodesLanguages() throws ServiceException;

  boolean removeChild(N parent, N child) throws ServiceException;

  /**
   * @param child newly created child node to be saved
   * @param parent parent node the new node is child of
   * @return saved child node
   * @throws ServiceException if saving fails
   */
  N saveWithParent(N child, N parent) throws ServiceException;

  boolean updateChildrenOrder(N parent, List<N> children) throws ServiceException;
}
