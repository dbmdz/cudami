package de.digitalcollections.model.jackson.file;

import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import org.junit.jupiter.api.Test;

public class MimeTypeTest extends BaseJsonSerializationTest {

  public MimeType createObject() {
    return MimeType.MIME_IMAGE_JPEG;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    MimeType mimeType = createObject();
    checkSerializeDeserialize(mimeType, "serializedTestObjects/file/MimeType.json");
  }
}
