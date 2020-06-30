package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface NodeService<N extends Node> extends IdentifiableService<N> {

  N getParent(N node);

  N getParent(UUID nodeUuid);

  List<N> getChildren(N node);

  List<N> getChildren(UUID nodeUuid);

  PageResponse<N> getChildren(UUID uuid, PageRequest pageRequest);

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
}
