package de.digitalcollections.model.jackson.identifiable.resource;

import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class LinkedDataFileResourceTest extends BaseJsonSerializationTest {

  private LinkedDataFileResource createObject() {
    LinkedDataFileResource resource = new LinkedDataFileResource();
    resource.setContext(URI.create("http://iiif.io/api/presentation/2/context.json"));
    resource.setObjectType("sc:Manifest");
    return resource;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    LinkedDataFileResource resource = createObject();
    checkSerializeDeserialize(
        resource, "serializedTestObjects/identifiable/resource/LinkedDataFileResource.json");
  }
}
