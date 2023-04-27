package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ResourceNotFoundException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
            .identifier(
                Identifier.builder()
                    .namespace("mdz-obj")
                    .id("bsb10000001")
                    .uuid("53e3e619-47a3-4110-84f7-acba12a52298")
                    .build())
            .label(
                "Actorum Bohemicorum, ... Theil, Das ist: Warhaffte vnd eigentliche Beschreibung aller fürnembsten vnd denckwürdigsten Historien vnd Geschichten, Welche sich im Königreich Böheim vnd dessen incorporirten Ländern ... begeben vnd zugetragen haben : Auß allerhand glaubwürdigen Publicis scriptis in eine feine richtige Ordnung zusammen verfasset, jetzo mit fleiß ubersehen, gemehret vnd auffs newe zugerichtet")
            .lastModified("2020-08-21T07:49:37.00445")
            .previewImage(
                "default.jpg",
                "abe16b03-c5d5-41a6-9475-f742e06ae881",
                "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
            .refId(72)
            .build();

    when(digitalObjectService.getByIdentifier(any(Identifier.class))).thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can filter by the parent UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/digitalobjects?pageNumber=0&pageSize=10000&filter=[parent_uuid]:eq:1c419226-8d61-4efa-923a-7fbaf961eb9d"
      })
  public void filterByParentUUID(String path) throws Exception {
    String parentUuidStr = "1c419226-8d61-4efa-923a-7fbaf961eb9d";
    UUID parentUuid = UUID.fromString(parentUuidStr);

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(10000);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("parent_uuid", true, FilterOperation.EQUALS, parentUuidStr);
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    PageResponse<DigitalObject> expected = new PageResponse<>();
    expected.setContent(
        List.of(
            DigitalObject.builder()
                .uuid("7593c90e-6fb7-49b4-a70b-032761c9bbcd")
                .created("2020-08-21T07:49:37.004443")
                .identifier(
                    Identifier.builder()
                        .namespace("mdz-obj")
                        .id("bsb10000001")
                        .uuid("53e3e619-47a3-4110-84f7-acba12a52298")
                        .build())
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

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/digitalobjects/identifier/foo:bar",
        "/v5/digitalobjects/identifier/foo:bar",
        "/v2/digitalobjects/identifier/foo:bar",
        "/latest/digitalobjects/identifier/foo:bar",
        "/v6/digitalobjects/identifier/foo:bar.json",
        "/v5/digitalobjects/identifier/foo:bar.json",
        "/v2/digitalobjects/identifier/foo:bar.json",
        "/latest/digitalobjects/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    DigitalObject expected = DigitalObject.builder().build();

    when(digitalObjectService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar").build())))
        .thenReturn(expected);

    testHttpGet(path);

    verify(digitalObjectService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/digitalobjects/identifier/", "/v5/digitalobjects/identifier/"})
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    DigitalObject expected = DigitalObject.builder().build();

    when(digitalObjectService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(digitalObjectService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }

  @DisplayName("throws a 404 exception, when update of a not yet existing resource is attempted")
  @Test
  public void updatingANonExistingResource() throws Exception {
    doThrow(ResourceNotFoundException.class)
        .when(digitalObjectService)
        .update(any(DigitalObject.class));

    String jsonBody =
        """
        {
          "identifiableObjectType" : "DIGITAL_OBJECT",
          "uuid" : "39427eac-a6ff-444d-9d2b-3673a8a0a53a"
        }
        """;

    testPutJsonWithState("/v6/digitalobjects/39427eac-a6ff-444d-9d2b-3673a8a0a53a", jsonBody, 404);
  }
}
