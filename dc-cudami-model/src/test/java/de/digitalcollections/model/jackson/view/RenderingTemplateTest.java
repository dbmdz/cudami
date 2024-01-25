package de.digitalcollections.model.jackson.view;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class RenderingTemplateTest extends BaseJsonSerializationTest {

  private RenderingTemplate createObject() {
    return RenderingTemplate.builder()
        .description(Locale.ENGLISH, "Template description")
        .label(Locale.ENGLISH, "Template label")
        .name("Template name")
        .uuid("2552a82e-8d5c-4bfd-87df-e2f185476885")
        .build();
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    RenderingTemplate template = createObject();
    checkSerializeDeserialize(template, "serializedTestObjects/view/RenderingTemplate.json");
  }
}
