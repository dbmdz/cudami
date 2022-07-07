package de.digitalcollections.cudami.server.controller.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("The abstract IdentifiableController")
class AbstractIdentifiableControllerTest {

  private TestAbstractIdentifiableController testAbstractIdentifiableController;

  private PersonService personService;

  @BeforeEach()
  public void beforeEach() {
    personService = mock(PersonService.class);
    testAbstractIdentifiableController = new TestAbstractIdentifiableController(personService);
  }

  @DisplayName("can filter after label only")
  @Test
  public void filterLabelOnly() {
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    testAbstractIdentifiableController.find(0, 1, null, null, "Label", null);
    verify(personService, times(1)).find(pageRequestArgumentCaptor.capture());

    PageRequest actual = pageRequestArgumentCaptor.getValue();

    assertThat(actual.getPageNumber()).isEqualTo(0);
    assertThat(actual.getPageSize()).isEqualTo(1);

    Filtering actualFiltering = actual.getFiltering();

    FilterCriterion<?> expectedFilterCriterion =
        FilterCriterion.builder().withExpression("label").contains("Label").build();
    assertThat(actualFiltering.getFilterCriteria()).containsExactly(expectedFilterCriterion);
  }

  @DisplayName("can filter after label only with a phrase search")
  @Test
  public void filterLabelOnlyPhraseSearch() {
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    testAbstractIdentifiableController.find(0, 1, null, null, "\"Label Label\"", null);
    verify(personService, times(1)).find(pageRequestArgumentCaptor.capture());

    PageRequest actual = pageRequestArgumentCaptor.getValue();

    assertThat(actual.getPageNumber()).isEqualTo(0);
    assertThat(actual.getPageSize()).isEqualTo(1);

    Filtering actualFiltering = actual.getFiltering();

    FilterCriterion<?> expectedFilterCriterion =
        FilterCriterion.builder().withExpression("label").isEquals("\"Label Label\"").build();
    assertThat(actualFiltering.getFilterCriteria()).containsExactly(expectedFilterCriterion);
  }

  @DisplayName("can filter after label and labelLanguage")
  @Test
  public void filterLabelAndLabelLanguage() {
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    testAbstractIdentifiableController.find(0, 1, null, null, "Label", Locale.GERMAN);
    verify(personService, times(1)).find(pageRequestArgumentCaptor.capture());

    PageRequest actual = pageRequestArgumentCaptor.getValue();

    assertThat(actual.getPageNumber()).isEqualTo(0);
    assertThat(actual.getPageSize()).isEqualTo(1);

    Filtering actualFiltering = actual.getFiltering();

    FilterCriterion<?> expectedFilterCriterion =
        FilterCriterion.builder().withExpression("label.de").contains("Label").build();
    assertThat(actualFiltering.getFilterCriteria()).containsExactly(expectedFilterCriterion);
  }

  @DisplayName("can filter after other filterCriteria")
  @Test
  public void filterOtherFilterCriteria() {
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    FilterCriterion<?> filterCriterionFoo = FilterCriterion.builder().isEquals("foovalue").build();
    FilterCriterion<?> filterCriterionBar = FilterCriterion.builder().contains("barvalue").build();
    testAbstractIdentifiableController.find(
        0, 1, null, null, null, null, "foo", filterCriterionFoo, "bar", filterCriterionBar);
    verify(personService, times(1)).find(pageRequestArgumentCaptor.capture());

    PageRequest actual = pageRequestArgumentCaptor.getValue();

    assertThat(actual.getPageNumber()).isEqualTo(0);
    assertThat(actual.getPageSize()).isEqualTo(1);

    Filtering actualFiltering = actual.getFiltering();

    FilterCriterion<?> expectedFilterCriterionFoo =
        FilterCriterion.builder().withExpression("foo").isEquals("foovalue").build();
    FilterCriterion<?> expectedFilterCriterionBar =
        FilterCriterion.builder().withExpression("bar").contains("barvalue").build();
    assertThat(actualFiltering.getFilterCriteria())
        .containsExactlyInAnyOrder(expectedFilterCriterionFoo, expectedFilterCriterionBar);
  }

  private class TestAbstractIdentifiableController extends AbstractIdentifiableController<Person> {

    private IdentifiableService<Person> personService;

    public TestAbstractIdentifiableController(PersonService personService) {
      this.personService = personService;
    }

    @Override
    protected IdentifiableService<Person> getService() {
      return personService;
    }
  }
}
