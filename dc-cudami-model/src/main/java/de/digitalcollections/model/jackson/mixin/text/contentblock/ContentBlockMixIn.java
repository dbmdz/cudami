package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.digitalcollections.model.text.contentblock.Blockquote;
import de.digitalcollections.model.text.contentblock.BulletList;
import de.digitalcollections.model.text.contentblock.CodeBlock;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.HardBreak;
import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.HorizontalRule;
import de.digitalcollections.model.text.contentblock.IFrame;
import de.digitalcollections.model.text.contentblock.Image;
import de.digitalcollections.model.text.contentblock.ListItem;
import de.digitalcollections.model.text.contentblock.OrderedList;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Table;
import de.digitalcollections.model.text.contentblock.TableCell;
import de.digitalcollections.model.text.contentblock.TableHeader;
import de.digitalcollections.model.text.contentblock.TableRow;
import de.digitalcollections.model.text.contentblock.Text;
import de.digitalcollections.model.text.contentblock.Video;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = Blockquote.class, name = "blockquote"),
  @JsonSubTypes.Type(value = BulletList.class, name = "bullet_list"),
  @JsonSubTypes.Type(value = CodeBlock.class, name = "code_block"),
  @JsonSubTypes.Type(value = HardBreak.class, name = "hard_break"),
  @JsonSubTypes.Type(value = Heading.class, name = "heading"),
  @JsonSubTypes.Type(value = HorizontalRule.class, name = "horizontal_rule"),
  @JsonSubTypes.Type(value = IFrame.class, name = "iframe"),
  @JsonSubTypes.Type(value = Image.class, name = "image"),
  @JsonSubTypes.Type(value = ListItem.class, name = "list_item"),
  @JsonSubTypes.Type(value = OrderedList.class, name = "ordered_list"),
  @JsonSubTypes.Type(value = Paragraph.class, name = "paragraph"),
  // see https://github.com/ProseMirror/prosemirror-tables/blob/master/src/schema.js
  @JsonSubTypes.Type(value = TableCell.class, name = "table_cell"),
  @JsonSubTypes.Type(value = TableHeader.class, name = "table_header"),
  @JsonSubTypes.Type(value = Table.class, name = "table"),
  @JsonSubTypes.Type(value = TableRow.class, name = "table_row"),
  @JsonSubTypes.Type(value = Text.class, name = "text"),
  @JsonSubTypes.Type(value = Video.class, name = "video")
})
public interface ContentBlockMixIn {

  @JsonProperty("content")
  List<ContentBlock> getContents();

  @JsonProperty("content")
  void setContents(List<ContentBlock> contents);

  @JsonIgnore
  void addContent(ContentBlock content);
}
