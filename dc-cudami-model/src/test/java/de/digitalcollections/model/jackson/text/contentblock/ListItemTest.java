package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.ListItem;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class ListItemTest extends BaseJsonSerializationTest {

  private ListItem createObject() {
    ListItem listItem = new ListItem();
    Paragraph paragraph = new Paragraph();
    Text content = new Text("This is a test.");
    paragraph.addContentBlock(content);
    listItem.addContentBlock(content);
    return listItem;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    ListItem listItem = createObject();
    checkSerializeDeserialize(listItem, "serializedTestObjects/text/contentblock/ListItem.json");
  }
}
