package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.DigitalObjectBuilder;
import de.digitalcollections.cudami.server.model.PageResponseBuilder;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(DigitalObjectController.class)
@DisplayName("The DigitalObjectController")
class DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("can return empty collections for a digital object")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/digitalobjects/51f9e6c9-4c91-4fdc-9563-26c17ff110cc/collections?active=true&pageNumber=0&pageSize=1000"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {

    PageResponse<Collection> expected =
        new PageResponseBuilder(Collection.class)
            .withoutContent()
            .forRequestPage(0)
            .forPageSize(1000)
            .forStartDate("c.publication_start", "2021-03-31")
            .forEndDate("c.publication_end", "2021-03-31")
            .build();

    DigitalObject digitalObject = new DigitalObjectBuilder().atPath(path).build();
    when(digitalObjectService.getActiveCollections(eq(digitalObject), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns a digital object by its identifier")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/digitalobjects/identifier/mdz-obj:bsb10000001",
        "/v2/digitalobjects/identifier/mdz-obj:bsb10000001.json"
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
}
