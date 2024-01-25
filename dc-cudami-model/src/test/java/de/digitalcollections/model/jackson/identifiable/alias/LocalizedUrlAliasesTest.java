package de.digitalcollections.model.jackson.identifiable.alias;

import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import org.junit.jupiter.api.Test;

public class LocalizedUrlAliasesTest extends BaseJsonSerializationTest {
  private LocalizedUrlAliases createObject() {
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(UrlAliasTest.createObject());
    return localizedUrlAliases;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    LocalizedUrlAliases localizedUrlAliases = createObject();
    checkSerializeDeserialize(
        localizedUrlAliases, "serializedTestObjects/identifiable/alias/LocalizedUrlAliases.json");
  }
}
