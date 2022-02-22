package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.CollectionBuilder;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.PageResponseBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3CollectionController.class)
@DisplayName("The V3 CollectionController")
public class V3CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;
  @MockBean private LocaleService localeService;

  @DisplayName("shall return digital objects for a collection")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/collections/a014a33b-6803-4b17-a876-a8f68758f2a7/digitalobjects?pageNumber=0&pageSize=1"
      })
  public void digitalObjectsForCollection(String path) throws Exception {
    SearchPageResponse<DigitalObject> expected =
        (SearchPageResponse<DigitalObject>)
            new SearchPageResponseBuilder()
                .forPageSize(1)
                .withTotalElements(319)
                .withContent(
                    List.of(
                        new DigitalObjectBuilder()
                            .createdAt("2020-09-29T10:58:30.458925")
                            .withIdentifier(
                                "mdz-obj", "bsb00000610", "5a4c1a74-40c9-4175-8f25-e5267eddaabc")
                            .withLabel(
                                "Die neuesten Schlager aus: Wenn Liebe erwacht : Hollandweibchen, Liebe im Schnee, Strohwitwe ... und viele andere ; Schlager u. Modelieder zum Mitsingen")
                            .lastModifiedAt("2020-09-29T10:58:30.458928")
                            .withPreviewImage(
                                "/iiif/image/v2/bsb00000610_00002/full/250,/0/default.jpg",
                                "94091a4d-c3ce-448c-93d1-7e86d8c3448a",
                                "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00000610_00002/full/250,/0/default.jpg")
                            .withUuid("66cdabfc-5b15-44f5-a01e-41aa8be2b9e2")
                            .withRefId(441)
                            .build()))
                .build();

    Collection collection =
        new CollectionBuilder().withUuid(extractFirstUuidFromPath(path)).build();

    when(collectionService.getDigitalObjects(eq(collection), any(SearchPageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("shall return subcollections")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/collections/a6193be6-1048-4c86-be54-7a99dbbb586c/subcollections?active=true&pageNumber=0&pageSize=1"
      })
  public void subcollections(String path) throws Exception {
    PageResponse<Collection> expected =
        new PageResponseBuilder(Collection.class)
            .forRequestPage(0)
            .forPageSize(1)
            .forStartDate("publicationStart", "2021-04-12")
            .forEndDate("publicationEnd", "2021-04-12")
            .withTotalElements(8)
            .withContent(
                new CollectionBuilder()
                    .createdAt("2020-07-10T12:12:33.099312")
                    .withDescription(
                        Locale.GERMAN,
                        "Mittelalterliche und neuzeitliche Handschriften aus aller Welt, Briefe und Autographen, Musikhandschriften")
                    .withDescription(
                        Locale.ENGLISH,
                        "Medieval and modern manuscripts from all over the world, letters and autographs, music manuscripts")
                    .withLabel(Locale.GERMAN, "Handschriften")
                    .withLabel(Locale.ENGLISH, "Manuscripts")
                    .lastModifiedAt("2020-11-05T17:00:27.181566")
                    .withPreviewImage(
                        "Hauptsammlung_Handschriften.jpg",
                        "fb167832-e1d2-4729-a3b9-f68af5fca0c3",
                        "file:///cudami/image/jpg/fb16/7832/e1d2/4729/a3b9/f68a/f5fc/a0c3/resource.jpg",
                        MimeType.MIME_IMAGE_JPEG,
                        "https://api.digitale-sammlungen.de/iiif/image/v2/fb167832-e1d2-4729-a3b9-f68af5fca0c3")
                    .withOpenPreviewImageInNewWindow()
                    .withUuid("888db95f-f837-4b17-bd3c-c00fd8c5205c")
                    .withRefId(115)
                    .withPublicationStart("2020-10-01")
                    .build())
            .build();

    when(collectionService.getActiveChildren(any(UUID.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("shall return active collections")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v3/collections?active=true&pageNumber=0&pageSize=1&sortBy=label_de.asc"})
  public void activeCollections(String path) throws Exception {
    PageResponse<Collection> expected =
        (PageResponse)
            new PageResponseBuilder()
                .forRequestPage(0)
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forStartDate("publicationStart", "2021-04-16")
                .forEndDate("publicationEnd", "2021-04-16")
                .withTotalElements(105)
                .withContent(
                    new CollectionBuilder()
                        .createdAt("2020-03-03T16:12:08.686626")
                        .withLabel(Locale.GERMAN, "Test")
                        .withLabel(Locale.ENGLISH, "test")
                        .lastModifiedAt("2020-10-19T17:04:07.889254")
                        .withPreviewImage(
                            "Test_Logo.jpg",
                            "cc3893e8-9530-4602-b640-118a3218e826",
                            "file:///cudami/image/jpg/cc38/93e8/9530/4602/b640/118a/3218/e826/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/cc3893e8-9530-4602-b640-118a3218e826")
                        .withAltText(Locale.GERMAN, "Test")
                        .withAltText(Locale.ENGLISH, "test")
                        .withTitle(Locale.GERMAN, "Test")
                        .withTitle(Locale.ENGLISH, "test")
                        .withOpenPreviewImageInNewWindow()
                        .withUuid("0b0b89e1-3f8a-4928-b8f3-67a8c4b3ff57")
                        .withRefId(14)
                        .withPublicationStart("2020-10-01")
                        .build())
                .build();
    when(collectionService.findActive(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v3/collections/active.json");
  }

  @DisplayName("shall search over collections")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v3/collections/search?pageSize=1", "/latest/collections/search?pageSize=1"})
  public void searchCollections(String path) throws Exception {
    SearchPageResponse<Collection> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder()
                .forRequestPage(0)
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(145)
                .withContent(
                    new CollectionBuilder()
                        .createdAt("2020-03-03T16:12:08.686626")
                        .withLabel(Locale.GERMAN, "Test")
                        .withLabel(Locale.ENGLISH, "test")
                        .lastModifiedAt("2020-10-19T17:04:07.889254")
                        .withPreviewImage(
                            "Test_Logo.jpg",
                            "cc3893e8-9530-4602-b640-118a3218e826",
                            "file:///cudami/image/jpg/cc38/93e8/9530/4602/b640/118a/3218/e826/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/cc3893e8-9530-4602-b640-118a3218e826")
                        .withAltText(Locale.GERMAN, "Test")
                        .withAltText(Locale.ENGLISH, "test")
                        .withTitle(Locale.GERMAN, "Test")
                        .withTitle(Locale.ENGLISH, "test")
                        .withOpenPreviewImageInNewWindow()
                        .withUuid("0b0b89e1-3f8a-4928-b8f3-67a8c4b3ff57")
                        .withRefId(14)
                        .withPublicationStart("2020-10-01")
                        .build())
                .build();
    when(collectionService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path, "/v3/collections/search.json");
  }
}
