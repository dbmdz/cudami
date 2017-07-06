package de.digitalcollections.cudami.server.backend.impl.neo4j.model.entity;

import de.digitalcollections.cudami.model.api.ContentBlock;
import java.util.List;
import org.neo4j.ogm.annotation.NodeEntity;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.TextContent;
import java.util.Locale;
import java.util.Map;

@NodeEntity(label = "Article")
public class ArticleImpl extends EntityImpl implements TextContent<Long> {

  List<ContentNode> categories;

  Map<Locale, List<ContentBlock>> contentBlocks;

  @Override
  public List<ContentNode> getContentNodes() {
    return categories;
  }

  @Override
  public void setContentNodes(List<ContentNode> categories) {
    this.categories = categories;
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
