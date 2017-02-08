package de.digitalcollections.cms.model.api.entity;

import de.digitalcollections.cms.model.api.ContentBlock;
import java.io.Serializable;
import java.util.List;

/**
 * Article is used for text content.
 *
 * @param <ID> unique id specifying instance
 */
public interface Article<ID extends Serializable> extends Entity<ID> {

  List<Category> getCategories();

  void setCategories(List<Category> categories);

  /**
   * @return (multilingual) text content
   */
  List<ContentBlock> getContentBlocks();

  /**
   * @param content the (multilingual) text content
   */
  void setContentBlocks(List<ContentBlock> content);
}
