package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.ContentBlock;
import de.digitalcollections.cudami.model.api.identifiable.ContentBlocksContainer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContentBlocksContainerImpl extends IdentifiableImpl implements ContentBlocksContainer {

  private Map<Locale, List<ContentBlock>> contentBlocks;

  @Override
  public Map<Locale, List<ContentBlock>> getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(Map<Locale, List<ContentBlock>> contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

}
