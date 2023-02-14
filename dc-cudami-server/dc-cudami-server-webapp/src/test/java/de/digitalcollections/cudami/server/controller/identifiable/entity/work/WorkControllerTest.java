package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.AgentService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(WorkController.class)
@DisplayName("The WorkController")
class WorkControllerTest extends BaseControllerTest {

  @MockBean private WorkService workService;
  @MockBean private ItemService itemService;
  @MockBean private AgentService agentService;

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/works/identifier/foo:bar",
        "/v5/works/identifier/foo:bar",
        "/v2/works/identifier/foo:bar",
        "/latest/works/identifier/foo:bar",
        "/v6/works/identifier/foo:bar.json",
        "/v5/works/identifier/foo:bar.json",
        "/v2/works/identifier/foo:bar.json",
        "/latest/works/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Work expected = new Work();

    when(workService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(workService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/works/identifier/",
        "/v5/works/identifier/",
        "/v2/works/identifier/",
        "/latest/works/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Work expected = new Work();

    when(workService.getByIdentifier(eq("foo"), eq("bar/bla"))).thenReturn(expected);

    testHttpGet(
        basePath
            + java.util.Base64.getEncoder()
                .encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(workService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }
}
