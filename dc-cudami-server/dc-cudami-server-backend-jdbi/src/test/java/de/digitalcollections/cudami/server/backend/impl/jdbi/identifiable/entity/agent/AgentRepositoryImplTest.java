package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {AgentRepositoryImpl.class})
@DisplayName("The Agent Repository")
class AgentRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<AgentRepositoryImpl<Agent>> {
  @BeforeEach
  public void beforeEach() {
    repo = new AgentRepositoryImpl<>(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can save (create) an agent")
  void testSave() {
    Agent agent = Agent.builder().label("Test").addName(Locale.ENGLISH, "a name").build();
    saveAndAssertTimestampsAndEqualityToSaveable(agent);
  }

  @Test
  @DisplayName("can update an agent")
  void testUpdate() {
    Agent agent = Agent.builder().label("Test").addName("some name").build();
    repo.save(agent);

    agent.setLabel("changed test");
    LocalizedText name =
        LocalizedText.builder()
            .text(Locale.ENGLISH, "some english name")
            .text(LOCALE_ZH_HANI, "難經辨眞")
            .build();
    agent.setName(name);
    agent.setNameLocalesOfOriginalScripts(Set.of(Locale.ENGLISH, LOCALE_ZH_HANI));

    Agent beforeUpdate = createDeepCopy(agent);
    updateAndAssertUpdatedLastModifiedTimestamp(agent);
    assertInDatabaseIsEqualToUpdateable(agent, beforeUpdate, Function.identity());
  }
}
