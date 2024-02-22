package io.github.dbmdz.cudami.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("The FileResources APIController")
class FileResourcesMetadataAPIControllerTest {

  private FileResourcesMetadataAPIController controller;
  private LanguageService languageService;
  private CudamiFileResourcesMetadataClient cudamiFileResourcesMetadataClient;

  @BeforeEach
  public void beforeEach() {
    languageService = mock(LanguageService.class);
    CudamiClient cudamiClient = mock(CudamiClient.class);
    cudamiFileResourcesMetadataClient = mock(CudamiFileResourcesMetadataClient.class);
    when(cudamiClient.forFileResourcesMetadata()).thenReturn(cudamiFileResourcesMetadataClient);
    controller = new FileResourcesMetadataAPIController(cudamiClient, languageService);
  }

  @DisplayName("transforms the searchTerm into a filtering")
  @Test
  public void transformSearchTermIntoFiltering() throws TechnicalException {
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(11);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterionLabel =
        new FilterCriterion("label", FilterOperation.CONTAINS, "foo");
    FilterCriterion filterCriterionDescription =
        new FilterCriterion("description", FilterOperation.CONTAINS, "foo");
    FilterCriterion filterCriterionFilename =
        new FilterCriterion("filename", FilterOperation.CONTAINS, "foo");
    Filtering filtering =
        Filtering.builder()
            .filterCriterion(FilterLogicalOperator.OR, filterCriterionLabel)
            .filterCriterion(FilterLogicalOperator.OR, filterCriterionDescription)
            .filterCriterion(FilterLogicalOperator.OR, filterCriterionFilename)
            .build();
    expectedPageRequest.setFiltering(filtering);

    controller.findByType("image", 0, 11, "foo", null);

    verify(cudamiFileResourcesMetadataClient, times(1))
        .findByType(pageRequestArgumentCaptor.capture(), eq("image"));
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }
}
