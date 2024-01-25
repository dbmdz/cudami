package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.BulletList;
import de.digitalcollections.model.text.contentblock.ListItem;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class BulletListTest extends BaseJsonSerializationTest {

  private BulletList createObject() {
    BulletList bulletList = new BulletList();
    ListItem item1 = new ListItem();
    Paragraph paragraph1 = new Paragraph();
    Text content1 = new Text("Punkt 1");
    paragraph1.addContentBlock(content1);
    item1.addContentBlock(paragraph1);
    bulletList.addContentBlock(item1);
    ListItem item2 = new ListItem();
    Paragraph paragraph2 = new Paragraph();
    Text content2 = new Text("Punkt 2");
    paragraph2.addContentBlock(content2);
    item2.addContentBlock(paragraph2);
    bulletList.addContentBlock(item2);
    return bulletList;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    BulletList bulletList = createObject();
    checkSerializeDeserialize(
        bulletList, "serializedTestObjects/text/contentblock/BulletList.json");
  }
}
