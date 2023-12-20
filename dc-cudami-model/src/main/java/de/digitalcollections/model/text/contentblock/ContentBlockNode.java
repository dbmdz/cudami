package de.digitalcollections.model.text.contentblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ContentBlockNode extends ContentBlock {

  protected List<ContentBlock> contentBlocks;

  public void addContentBlock(ContentBlock contentBlock) {
    if (contentBlocks == null) {
      contentBlocks = new ArrayList<>(0);
    }

    contentBlocks.add(contentBlock);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ContentBlockNode)) {
      return false;
    }
    ContentBlockNode that = (ContentBlockNode) o;
    return Objects.equals(contentBlocks, that.contentBlocks);
  }

  public List<ContentBlock> getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentBlocks);
  }

  public void setContentBlocks(List<ContentBlock> contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "contentBlocks="
        + contentBlocks
        + ", "
        + "hashCode="
        + hashCode()
        + '}';
  }
}
