package de.digitalcollections.model.view;

import java.util.ArrayList;
import java.util.List;

/**
 * The breadcrumb navigation.
 *
 * <p>It consists of an ordered list of {@link BreadcrumbNode}s, representing navigation items,
 * where the first one is the root item and the last one is the navigation item for the current
 * location.
 */
public class BreadcrumbNavigation {

  private List<BreadcrumbNode> navigationItems = new ArrayList<>(0);

  public BreadcrumbNavigation() {}

  public BreadcrumbNavigation(List<BreadcrumbNode> navigationItems) {
    this.navigationItems = navigationItems;
  }

  /**
   * An ordered list of Nodes, each of them represents one navigation item with a label and an
   * object to get link id from (depends on your underlying model/objects)
   *
   * @return ordered list, the first item is the root location
   */
  public List<BreadcrumbNode> getNavigationItems() {
    return navigationItems;
  }
}
