package de.digitalcollections.model.jackson.identifiable.entity.digitalobject;

import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.CreationInfo;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The CreationInfo")
public class CreationInfoTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    CreationInfo creationInfo =
        CreationInfo.builder()
            .creator(CorporateBody.builder().label(Locale.GERMAN, "Acme Inc.").build())
            .geoLocation(HumanSettlement.builder().label(Locale.GERMAN, "Dorfstadt").build())
            .date("2022-01-01")
            .build();

    checkSerializeDeserialize(
        creationInfo, "serializedTestObjects/identifiable/entity/digitalobject/CreationInfo.json");
  }
}
