package de.digitalcollections.model.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Agent")
class AgentTest {

  @DisplayName("can create an instance with the help of its inner class builder")
  @Test
  public void testBuilder() {
    Agent agent =
        Agent.builder()
            .uuid(UUID.randomUUID())
            .description(Locale.GERMAN, "Beispiel-Bild")
            .description(Locale.ENGLISH, "Example Image")
            .label(Locale.GERMAN, "Beispielbild")
            .label(Locale.ENGLISH, "Example Image")
            .created("2021-01-13T12:34:54")
            .lastModified("2021-01-14T02:45:24")
            .primaryLocalizedUrlAlias(Locale.GERMAN, "blubb")
            .build();
    assertThat(agent).isExactlyInstanceOf(Agent.class);
  }
}
