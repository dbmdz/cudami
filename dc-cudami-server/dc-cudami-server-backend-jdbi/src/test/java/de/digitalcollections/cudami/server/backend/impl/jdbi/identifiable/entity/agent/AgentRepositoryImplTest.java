package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.Set;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {AgentRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Agent Repository")
class AgentRepositoryImplTest {

  AgentRepositoryImpl<Agent> repo;

  private static final Locale LOCALE_ZH_HANI =
      new Locale.Builder().setLanguage("zh").setScript("Hani").build();

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new AgentRepositoryImpl<>(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can save (create) an agent")
  void testCreate() {
    Agent agent = Agent.builder().label("Test").addName(Locale.ENGLISH, "a name").build();

    Agent saved = repo.save(agent);
    Agent actual = repo.getByUuid(saved.getUuid());

    assertThat(actual).isEqualTo(agent);
  }

  @Test
  @DisplayName("can update an agent")
  void testUpdate() {
    Agent agent = repo.save(Agent.builder().label("Test").addName("some name").build());
    agent.setLabel("changed test");
    LocalizedText name =
        LocalizedText.builder()
            .text(Locale.ENGLISH, "some english name")
            .text(LOCALE_ZH_HANI, "難經辨眞")
            .build();
    agent.setName(name);
    agent.setNameLocalesOfOriginalScripts(Set.of(Locale.ENGLISH, LOCALE_ZH_HANI));

    Agent saved = repo.update(agent);
    Agent actual = repo.getByUuid(saved.getUuid());

    assertThat(actual).isEqualTo(agent);
  }
}
