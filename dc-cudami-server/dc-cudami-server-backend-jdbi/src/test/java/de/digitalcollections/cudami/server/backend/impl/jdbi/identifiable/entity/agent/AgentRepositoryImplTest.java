package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
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
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Agent Repository")
class AgentRepositoryImplTest {

  AgentRepositoryImpl<Agent> repo;

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
    Agent agent = Agent.builder().label("Test").build();

    Agent actual = repo.save(agent);

    assertThat(actual).isEqualTo(agent);
  }

  @Test
  @DisplayName("can update an agent")
  void testUpdate() {
    Agent agent = repo.save(Agent.builder().label("Test").build());
    agent.setLabel("changed test");

    Agent actual = repo.update(agent);

    assertThat(actual).isEqualTo(agent);
  }
}
