package de.digitalcollections.cms.model.api.entity;

import java.util.List;

/**
 * Category is used to structure content hierarchically.
 */
public interface Category extends Entity {

  Category getParent();

  void setParent(Category category);

  List<Category> getChildren();

  void setChildren(List<Category> categories);

  void addChild(Category category);
}
