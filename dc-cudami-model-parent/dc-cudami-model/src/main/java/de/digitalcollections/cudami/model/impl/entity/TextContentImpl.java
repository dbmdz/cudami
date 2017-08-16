package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.ContentBlock;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.TextContent;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextContentImpl extends EntityImpl implements TextContent<Long> {

  private List<ContentNode> contentNodes;
  private Map<Locale, List<ContentBlock>> contentBlocks;

  @Override
  public List<ContentNode> getContentNodes() {
    return contentNodes;
  }

  @Override
  public void setContentNodes(List<ContentNode> contentNodes) {
    this.contentNodes = contentNodes;
  }

  @Override
  public Map<Locale, List<ContentBlock>> getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(Map<Locale, List<ContentBlock>> contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

}
