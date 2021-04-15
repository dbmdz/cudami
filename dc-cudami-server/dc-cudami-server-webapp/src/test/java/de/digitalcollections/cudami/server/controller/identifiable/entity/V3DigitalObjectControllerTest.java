package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.CollectionBuilder;
import de.digitalcollections.cudami.server.model.PageResponseBuilder;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3DigitalObjectController.class)
@DisplayName("The DigitalObjectController V3")
public class V3DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("can return the collections, a digital object belongs to")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7/collections?active=true&pageNumber=0&pageSize=1"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {
    PageResponse<Collection> expected =
        (PageResponse)
            new PageResponseBuilder<>()
                .forRequestPage(0)
                .forPageSize(1)
                .forStartDate("c.publication_start", "2021-04-12")
                .forEndDate("c.publication_end", "2021-04-12")
                .withTotalElements(1)
                .withContent(
                    List.of(
                        new CollectionBuilder()
                            .createdAt("2020-07-16T11:51:33.981829")
                            .withLabel(Locale.GERMAN, "Lateinische Handschriften")
                            .withLabel(Locale.ENGLISH, "Latin Manuscripts")
                            .lastModifiedAt("2020-11-04T15:46:42.81741")
                            .withPreviewImage(
                                "Lateinische_Handschriften_bsb00131281_27.jpg",
                                "2780bee1-eeec-4b50-a95b-bba90793fc6a",
                                "file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg",
                                MimeType.MIME_IMAGE_JPEG,
                                "https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a")
                            .withOpenPreviewImageInNewWindow()
                            .withUuid("25198d8b-38d4-49f7-9ef0-d99b3e607e30")
                            .withRefId(148)
                            .withPublicationStart("2020-11-01")
                            .build()))
                .build();

    when(digitalObjectService.getActiveCollections(
            any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }
}
