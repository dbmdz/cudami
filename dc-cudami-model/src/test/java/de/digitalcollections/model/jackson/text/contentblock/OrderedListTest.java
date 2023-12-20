package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.ListItem;
import de.digitalcollections.model.text.contentblock.OrderedList;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class OrderedListTest extends BaseJsonSerializationTest {

  private OrderedList createObject() {
    OrderedList orderedList = new OrderedList();
    orderedList.addAttribute("order", 1);
    ListItem item1 = new ListItem();
    Paragraph paragraph1 = new Paragraph();
    Text content1 = new Text("Punkt 1");
    paragraph1.addContentBlock(content1);
    item1.addContentBlock(paragraph1);
    orderedList.addContentBlock(item1);
    ListItem item2 = new ListItem();
    Paragraph paragraph2 = new Paragraph();
    Text content2 = new Text("Punkt 2");
    paragraph2.addContentBlock(content2);
    item2.addContentBlock(paragraph2);
    orderedList.addContentBlock(item2);
    return orderedList;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    OrderedList orderedList = createObject();
    checkSerializeDeserialize(
        orderedList, "serializedTestObjects/text/contentblock/OrderedList.json");
  }
}
