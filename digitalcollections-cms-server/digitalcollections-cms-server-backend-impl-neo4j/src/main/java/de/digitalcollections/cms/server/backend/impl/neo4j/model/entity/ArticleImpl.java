package de.digitalcollections.cms.server.backend.impl.neo4j.model.entity;

import de.digitalcollections.cms.model.api.ContentBlock;
import de.digitalcollections.cms.model.api.entity.Article;
import de.digitalcollections.cms.model.api.entity.Category;
import java.util.List;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Article")
public class ArticleImpl extends EntityImpl implements Article<Long> {

  List<Category> categories;

  List<ContentBlock> contentBlocks;

  @Override
  public List<Category> getCategories() {
    return categories;
  }

  @Override
  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  @Override
  public List<ContentBlock> getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(List<ContentBlock> contentBlocks) {
    this.contentBlocks = contentBlocks;
  }
}
