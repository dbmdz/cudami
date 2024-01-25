package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class TopicTest extends BaseJsonSerializationTest {

  private static LocalizedText createLabel(Locale locale, String text) {
    return new LocalizedText(locale, text);
  }

  private LocalizedStructuredContent createDescription(Locale locale, String text) {
    LocalizedStructuredContent localizedStructuredContent = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    ContentBlock contentBlock = new Paragraph(text);
    structuredContent.addContentBlock(contentBlock);
    localizedStructuredContent.put(locale, structuredContent);
    return localizedStructuredContent;
  }

  private Topic createObject() {
    Topic topic = new Topic();
    topic.setLabel(createLabel(Locale.ENGLISH, "King Ludwig II of Bavaria and his Times"));
    topic.setDescription(
        createDescription(
            Locale.ENGLISH,
            "King Ludwig II (1845-1886) is one of the most famous figures of Bavarian history. By means of his castles and buildings, this theme attests to the splendid, colourful side of the \"Märchenkönig\" (fairy tale king). Nevertheless, less well-known aspects of Ludwig’s life are also illustrated, for example artistic influences and models or his relationship to the Bayerische Staatsbibliothek. Specialists receive information on early biographies of Ludwig II and about sources on his life."));
    Topic subtopic1 =
        createSubtopic(Locale.ENGLISH, "Sources about the Life and Times of Ludwig II", "");
    topic.addChild(subtopic1);
    Topic subtopic2 = createSubtopic(Locale.ENGLISH, "Artistic Influences and Models", "");
    topic.addChild(subtopic2);
    Topic subtopic3 = createSubtopic(Locale.ENGLISH, "Castles and Constructions of Ludwig II", "");
    topic.addChild(subtopic3);
    Topic subtopic4 =
        createSubtopic(
            Locale.ENGLISH,
            "Ludwig II and the Bayerische Staatsbibliothek",
            "King Ludwig II (1845-1886) left his mark on the Bayerische Staatsbibliothek in the form of a large number of diverse and noteworthy sources and materials ranging from the tenth to the nineteenth centuries.");
    topic.addChild(subtopic4);
    return topic;
  }

  private Topic createSubtopic(Locale locale, String label, String description) {
    Topic subtopic = new Topic();
    subtopic.setLabel(createLabel(locale, label));
    subtopic.setDescription(createDescription(locale, description));
    return subtopic;
  }

  /* rendered json: see test resource "topic.json" */
  @Test
  public void testSerializeDeserialize() throws Exception {
    Topic topic = createObject();
    checkSerializeDeserialize(topic, "serializedTestObjects/identifiable/entity/Topic.json");
  }
}
