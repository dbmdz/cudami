package de.digitalcollections.model.jackson.identifiable.entity.manifestation;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlementType;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Publisher")
public class PublisherTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    Publisher publication =
        Publisher.builder()
            .locations(
                List.of(
                    HumanSettlement.builder()
                        .humanSettlementType(HumanSettlementType.CITY)
                        .title(Locale.GERMAN, "München")
                        .label(Locale.GERMAN, "Stadt: München")
                        .build(),
                    HumanSettlement.builder()
                        .humanSettlementType(HumanSettlementType.CITY)
                        .title(Locale.GERMAN, "Hamburg")
                        .label(Locale.GERMAN, "Stadt: Hamburg")
                        .build()))
            .agent(
                Person.builder()
                    .title(Locale.GERMAN, "Karl Ranseier")
                    .label(
                        Locale.GERMAN, "Karl Ranseier, der erfolgloseste Herausgeber aller Zeiten")
                    .build())
            .datePresentation("1929 - 2021")
            .build();

    checkSerializeDeserialize(
        publication, "serializedTestObjects/identifiable/entity/manifestation/Publisher.json");
  }
}
