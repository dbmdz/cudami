package de.digitalcollections.model.jackson.identifiable.entity.agent;

import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class CorporateBodyTest extends BaseJsonSerializationTest {

  private CorporateBody createObject() {
    CorporateBody corporateBody = new CorporateBody();
    corporateBody.setLabel(new LocalizedText(Locale.GERMAN, "Bayerische Staatsbibliothek"));
    return corporateBody;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    CorporateBody corporateBody = createObject();
    checkSerializeDeserialize(
        corporateBody, "serializedTestObjects/identifiable/entity/agent/CorporateBody.json");
  }
}
