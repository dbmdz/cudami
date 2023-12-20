package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import java.util.List;

public interface ContentBlockNodeMixin {

  @JsonProperty("content")
  List<ContentBlock> getContentBlocks();

  @JsonProperty("content")
  void setContentBlocks(List<ContentBlock> contentBlocks);

  @JsonIgnore
  void addContentBlock(ContentBlock contentBlock);
}
