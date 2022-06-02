package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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
        DigitalObject.builder()
            .uuid("1c419226-8d61-4efa-923a-7fbaf961eb9d")
            .created("2020-08-21T07:49:37.004443")
            .identifier("mdz-obj", "bsb10000001", "53e3e619-47a3-4110-84f7-acba12a52298")
            .label(
                "Actorum Bohemicorum, ... Theil, Das ist: Warhaffte vnd eigentliche Beschreibung aller fürnembsten vnd denckwürdigsten Historien vnd Geschichten, Welche sich im Königreich Böheim vnd dessen incorporirten Ländern ... begeben vnd zugetragen haben : Auß allerhand glaubwürdigen Publicis scriptis in eine feine richtige Ordnung zusammen verfasset, jetzo mit fleiß ubersehen, gemehret vnd auffs newe zugerichtet")
            .lastModified("2020-08-21T07:49:37.00445")
            .previewImage(
                "default.jpg",
                "abe16b03-c5d5-41a6-9475-f742e06ae881",
                "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
            .refId(72)
            .build();

    when(digitalObjectService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can filter by the parent UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/digitalobjects?pageNumber=0&pageSize=10000&parent.uuid=eq:1c419226-8d61-4efa-923a-7fbaf961eb9d"
      })
  public void filterByParentUUID(String path) throws Exception {
    UUID parentUuid = UUID.fromString("1c419226-8d61-4efa-923a-7fbaf961eb9d");

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(10000);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("parent.uuid", FilterOperation.EQUALS, parentUuid);
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    PageResponse<DigitalObject> expected = new PageResponse<>();
    expected.setContent(
        List.of(
            DigitalObject.builder()
                .uuid("7593c90e-6fb7-49b4-a70b-032761c9bbcd")
                .created("2020-08-21T07:49:37.004443")
                .identifier("mdz-obj", "bsb10000001", "53e3e619-47a3-4110-84f7-acba12a52298")
                .label("Label")
                .lastModified("2020-08-21T07:49:37.00445")
                .previewImage(
                    "default.jpg",
                    "abe16b03-c5d5-41a6-9475-f742e06ae881",
                    "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
                .refId(72)
                .parent(DigitalObject.builder().uuid(parentUuid).build())
                .build()));
    expected.setRequest(expectedPageRequest);

    when(digitalObjectService.find(any(PageRequest.class))).thenReturn(expected);

    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    testJson(path, "/v6/digitalobjects/filtered_by_parent.json");

    verify(digitalObjectService, times(1)).find(pageRequestArgumentCaptor.capture());
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }
}
