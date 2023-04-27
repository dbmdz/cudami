package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(PersonController.class)
@DisplayName("The PersonController")
class PersonControllerTest extends BaseControllerTest {

  @MockBean private PersonService personService;
  @MockBean private WorkService workService;

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/persons/identifier/foo:bar",
        "/v5/persons/identifier/foo:bar",
        "/v2/persons/identifier/foo:bar",
        "/latest/persons/identifier/foo:bar",
        "/v6/persons/identifier/foo:bar.json",
        "/v5/persons/identifier/foo:bar.json",
        "/v2/persons/identifier/foo:bar.json",
        "/latest/persons/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Person expected = Person.builder().build();

    when(personService.getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build())))
        .thenReturn(expected);

    testHttpGet(path);

    verify(personService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/persons/identifier/",
        "/v5/persons/identifier/",
        "/v2/persons/identifier/",
        "/latest/persons/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Person expected = Person.builder().build();

    when(personService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(personService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }

  @DisplayName("can retrieve by localized exact label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=label_und-Latn:eq:\"Karl Ranseier\""})
  public void findByLocalizedExactLabel(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label_und-Latn")
                            .isEquals("\"Karl Ranseier\"")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by localized 'like' label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=label_und-Latn:like:Karl Ranseier"})
  public void findByLocalizedLikeLabel(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label_und-Latn")
                            .contains("Karl Ranseier")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by localized exact name")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=name_de:eq:\"Karl Ranseier\""})
  public void findByLocalizedExactName(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("name_de")
                            .isEquals("\"Karl Ranseier\"")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by localized 'like' name")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=name_und-Latn:like:Karl Ranseier"})
  public void findByLocalizedLikeName(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("name_und-Latn")
                            .contains("Karl Ranseier")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by non-localized exact name")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=name:eq:\"Karl Ranseier\""})
  public void findByNonLocalizedExactName(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("name")
                            .isEquals("\"Karl Ranseier\"")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by non-localized 'like' name")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/persons?filter=name:like:Karl Ranseier"})
  public void findByNonLocalizedLikeName(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(5)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("name")
                            .contains("Karl Ranseier")
                            .build())
                    .build())
            .build();
    verify(personService, times(1)).find(eq(expectedPageRequest));
  }
}
