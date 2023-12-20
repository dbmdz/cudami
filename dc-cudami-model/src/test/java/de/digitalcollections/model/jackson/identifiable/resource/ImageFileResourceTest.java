package de.digitalcollections.model.jackson.identifiable.resource;

import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import org.junit.jupiter.api.Test;

public class ImageFileResourceTest extends BaseJsonSerializationTest {

  private ImageFileResource createObject() {
    ImageFileResource image = new ImageFileResource();
    image.setHeight(768);
    image.setWidth(1024);
    return image;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    ImageFileResource image = createObject();
    checkSerializeDeserialize(
        image, "serializedTestObjects/identifiable/resource/ImageFileResource.json");
  }
}
