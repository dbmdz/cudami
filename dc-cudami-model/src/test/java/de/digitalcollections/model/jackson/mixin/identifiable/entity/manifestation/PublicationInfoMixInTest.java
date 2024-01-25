package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The PublicationInfo MixIn")
class PublicationInfoMixInTest {

  @DisplayName("does not serialize the 'empty' field")
  @Test
  public void doesNotSerializeEmpty() throws JsonProcessingException {
    ObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

    PublicationInfo publicationInfo =
        PublicationInfo.builder()
            .publisher(
                Publisher.builder()
                    .location(
                        HumanSettlement.builder()
                            .name(new LocalizedText(Locale.GERMAN, "München"))
                            .build())
                    .agent(
                        CorporateBody.builder()
                            .name(new LocalizedText(Locale.GERMAN, "Acme Inc."))
                            .build())
                    .build())
            .datePresentation("1929-1931")
            .build();

    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(publicationInfo);

    assertThat(json).doesNotContain("empty");
  }
}
