package de.digitalcollections.model.jackson.identifiable.versioning;

import de.digitalcollections.model.identifiable.versioning.Status;
import de.digitalcollections.model.identifiable.versioning.Version;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class VersionTest extends BaseJsonSerializationTest {

  private Version createObject() {
    Version version = new Version();
    version.setUuid(UUID.fromString("1e2d8b1e-c29d-475b-8f61-67b22ca6de89"));
    version.setVersionValue(0);
    version.setStatus(Status.INITIAL);
    return version;
  }

  @Test
  public void testSerialisationInBothWays() throws Exception {
    Version version = createObject();
    checkSerializeDeserialize(
        version, "serializedTestObjects/identifiable/versioning/Version.json");
  }
}
