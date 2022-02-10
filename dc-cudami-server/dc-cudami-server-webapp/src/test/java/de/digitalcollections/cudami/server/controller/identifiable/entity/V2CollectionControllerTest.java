package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.CollectionBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.PageResponseBuilder;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2CollectionController.class)
@DisplayName("The V2 CollectionController")
public class V2CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;
  @MockBean private LocaleService localeService;

  @DisplayName("shall return a collection list, optional with active filtering")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/collections/?pageNumber=0&pageSize=1",
        "/v2/collections/?pageNumber=0&pageSize=1&active=true"
      })
  public void collectionList(String path) throws Exception {
    PageResponse<Collection> expected =
        new PageResponseBuilder<>()
            .forPageSize(1)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .withTotalElements(139)
            .withContent(
                new CollectionBuilder()
                    .withUuid("0b0b89e1-3f8a-4928-b8f3-67a8c4b3ff57")
                    .createdAt("2020-03-03T16:12:08.686626")
                    .withLabel(
                        Locale.GERMAN,
                        "100(0) Schlüsseldokumente - zur deutschen Geschichte im 20. Jahrhundert sowie zur russischen und sowjetischen Geschichte (1917-1991)")
                    .withLabel(
                        Locale.ENGLISH,
                        "100(0) Key Documents on German History of the 20th Century and of the Russian and Soviet History (1917-1991)")
                    .lastModifiedAt("2020-10-19T17:04:07.889254")
                    .withPreviewImage(
                        "1000_Schluesseldokumente_neutrales_Logo.jpg",
                        "cc3893e8-9530-4602-b640-118a3218e826",
                        "file:///cudami/image/jpg/cc38/93e8/9530/4602/b640/118a/3218/e826/resource.jpg",
                        MimeType.MIME_IMAGE_JPEG,
                        "https://api.digitale-sammlungen.de/iiif/image/v2/cc3893e8-9530-4602-b640-118a3218e826")
                    .withAltTextFromLabel()
                    .withTitleFromLabel()
                    .withOpenPreviewImageInNewWindow()
                    .withRefId(14)
                    .withPublicationStart("2020-10-01")
                    .build())
            .build();

    when(collectionService.find(any(PageRequest.class))).thenReturn(expected);
    when(collectionService.findActive(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v2/collections/collectionlist.json");
  }

  @DisplayName("shall filter out inactive collections from the collection list")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/collections/?pageNumber=0&pageSize=1&active=true"})
  public void collectionListForInactive(String path) throws Exception {
    PageResponse<Collection> expected =
        new PageResponseBuilder<>()
            .forPageSize(1)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .withoutContent()
            .build();

    when(collectionService.findActive(any(PageRequest.class))).thenReturn(expected);
    testJson(path, "/v2/collections/collectionlist_inactive.json");
  }
}
