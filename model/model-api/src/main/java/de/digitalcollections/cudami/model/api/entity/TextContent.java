package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.ContentBlock;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * TextContent is used for text content.
 * @param <ID> unique serializable identifier
 */
public interface TextContent<ID extends Serializable> extends Entity<ID> {

  List<ContentNode> getContentNodes();

  void setContentNodes(List<ContentNode> contentNodes);

  /**
   * @return (multilingual) text content
   */
  Map<Locale, List<ContentBlock>> getContentBlocks();

  /**
   * @param contentBlocks the (multilingual) text content
   */
  void setContentBlocks(Map<Locale, List<ContentBlock>> contentBlocks);

  default void addContentBlocks(Locale locale, List<ContentBlock> contentBlocks) {
    if (getContentBlocks() == null) {
      setContentBlocks(new HashMap<>());
    }
    getContentBlocks().put(locale, contentBlocks);
  }
}
