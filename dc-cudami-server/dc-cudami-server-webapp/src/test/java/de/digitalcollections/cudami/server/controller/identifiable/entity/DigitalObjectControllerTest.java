package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(DigitalObjectController.class)
@DisplayName("The DigitalObjectController")
class DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("returns a digital object by its identifier")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/digitalobjects/identifier/mdz-obj:bsb10000001",
        "/latest/digitalobjects/identifier/mdz-obj:bsb10000001.json",
        "/v2/digitalobjects/identifier/mdz-obj:bsb10000001",
        "/v2/digitalobjects/identifier/mdz-obj:bsb10000001.json",
        "/v5/digitalobjects/identifier/mdz-obj:bsb10000001",
        "/v5/digitalobjects/identifier/mdz-obj:bsb10000001.json"
      })
  public void getDigitalObjectByIdentifier(String path) throws Exception {

    DigitalObject expected =
        new DigitalObjectBuilder()
            .withUuid("1c419226-8d61-4efa-923a-7fbaf961eb9d")
            .createdAt("2020-08-21T07:49:37.004443")
            .withIdentifier("mdz-obj", "bsb10000001", "53e3e619-47a3-4110-84f7-acba12a52298")
            .withLabel(
                "Actorum Bohemicorum, ... Theil, Das ist: Warhaffte vnd eigentliche Beschreibung aller fürnembsten vnd denckwürdigsten Historien vnd Geschichten, Welche sich im Königreich Böheim vnd dessen incorporirten Ländern ... begeben vnd zugetragen haben : Auß allerhand glaubwürdigen Publicis scriptis in eine feine richtige Ordnung zusammen verfasset, jetzo mit fleiß ubersehen, gemehret vnd auffs newe zugerichtet")
            .lastModifiedAt("2020-08-21T07:49:37.00445")
            .withPreviewImage(
                "default.jpg",
                "abe16b03-c5d5-41a6-9475-f742e06ae881",
                "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
            .withRefId(72)
            .build();

    when(digitalObjectService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can filter by the parent UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/digitalobjects/search?pageNumber=0&pageSize=10000&parent.uuid=eq:1c419226-8d61-4efa-923a-7fbaf961eb9d"
      })
  public void filterByParentUUID(String path) throws Exception {
    UUID parentUuid = UUID.fromString("1c419226-8d61-4efa-923a-7fbaf961eb9d");

    SearchPageResponse<DigitalObject> expected = new SearchPageResponse<>();
    expected.setContent(
        List.of(
            new DigitalObjectBuilder()
                .withUuid("7593c90e-6fb7-49b4-a70b-032761c9bbcd")
                .createdAt("2020-08-21T07:49:37.004443")
                .withIdentifier("mdz-obj", "bsb10000001", "53e3e619-47a3-4110-84f7-acba12a52298")
                .withLabel("Label")
                .lastModifiedAt("2020-08-21T07:49:37.00445")
                .withPreviewImage(
                    "default.jpg",
                    "abe16b03-c5d5-41a6-9475-f742e06ae881",
                    "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
                .withRefId(72)
                .withParent(new DigitalObjectBuilder().withUuid(parentUuid).build())
                .build()));

    when(digitalObjectService.find(any(SearchPageRequest.class))).thenReturn(expected);

    ArgumentCaptor<SearchPageRequest> searchPageRequestArgumentCaptor =
        ArgumentCaptor.forClass(SearchPageRequest.class);

    SearchPageRequest expectedSearchPageRequest = new SearchPageRequest();
    expectedSearchPageRequest.setPageSize(10000);
    expectedSearchPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("parent.uuid", FilterOperation.EQUALS, parentUuid);
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedSearchPageRequest.setFiltering(filtering);

    testJson(path, "/v5/digitalobjects/filtered_by_parent.json");

    verify(digitalObjectService, times(1)).find(searchPageRequestArgumentCaptor.capture());
    assertThat(searchPageRequestArgumentCaptor.getValue()).isEqualTo(expectedSearchPageRequest);
  }
}
