package de.digitalcollections.model.jackson.identifiable.entity.manifestation;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.time.TimeValue;
import de.digitalcollections.model.time.TimeValueRange;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The PublicationInfo")
public class PublicationInfoTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    PublicationInfo publicationInfo =
        PublicationInfo.builder()
            .datePresentation("2020")
            .navDateRange(
                new LocalDateRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31")))
            .timeValueRange(
                new TimeValueRange(
                    new TimeValue(
                        2020,
                        0,
                        0,
                        0,
                        0,
                        0,
                        TimeValue.PREC_YEAR,
                        0,
                        0,
                        0,
                        TimeValue.CM_GREGORIAN_PRO),
                    new TimeValue(
                        2020,
                        0,
                        0,
                        0,
                        0,
                        0,
                        TimeValue.PREC_YEAR,
                        0,
                        0,
                        0,
                        TimeValue.CM_GREGORIAN_PRO)))
            .publishers(
                List.of(
                    buildPublisher("Karl Ranseier", List.of("Köln")),
                    buildPublisher("Hans Dampf", List.of("Frankfurt", "München")),
                    buildPublisher(null, List.of("München", "Berlin")),
                    buildPublisher("Max Moritz", null)))
            .build();

    checkSerializeDeserialize(
        publicationInfo,
        "serializedTestObjects/identifiable/entity/manifestation/PublicationInfo.json");
  }

  // ---------------------------------------------------------------------
  private Publisher buildPublisher(String personName, List<String> cityNames) {
    List<String> presentationParts = new ArrayList<>();
    if (cityNames != null) {
      presentationParts.add(cityNames.stream().collect(Collectors.joining(", ")));
    }
    if (personName != null) {
      presentationParts.add(personName);
    }

    return Publisher.builder()
        .agent(
            personName != null
                ? Person.builder().label(personName).title(Locale.GERMAN, personName).build()
                : null)
        .locations(
            cityNames != null
                ? cityNames.stream()
                    .map(c -> HumanSettlement.builder().label(c).title(Locale.GERMAN, c).build())
                    .collect(Collectors.toList())
                : null)
        .build();
  }
}
