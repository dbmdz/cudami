package de.digitalcollections.model.jackson.legal;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.text.LocalizedText;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LicenseTest extends BaseJsonSerializationTest {

  private License createObject() throws MalformedURLException {
    License license = new License();
    license.setAcronym("CC0 1.0");
    license.setUuid(UUID.fromString("1e2d8b1e-c29d-475b-8f61-67b22ca6de89"));
    license.setCreated(LocalDateTime.of(2000, 1, 1, 10, 15));
    license.setLabel(new LocalizedText(Locale.ENGLISH, "Public Domain"));
    license.setLastModified(LocalDateTime.of(2015, 1, 1, 10, 45));
    license.setUrl(URI.create("http://rightsstatements.org/vocab/InC-NC/1.0/").toURL());
    return license;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    License license = createObject();
    checkSerializeDeserialize(license, "serializedTestObjects/legal/License.json");
  }
}
