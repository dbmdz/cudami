package de.digitalcollections.cms.model.api.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Category is used to structure content hierarchically.
 *
 * @param <ID> unique id specifying instance
 */
public interface Category<ID extends Serializable> extends Entity<ID> {

  Category getParent();

  void setParent(Category category);

  List<Category> getChildren();

  void setChildren(List<Category> categories);

  void addChild(Category category);
}
