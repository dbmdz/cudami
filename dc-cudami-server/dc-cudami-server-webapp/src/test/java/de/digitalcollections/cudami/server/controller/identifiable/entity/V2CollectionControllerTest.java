package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.paging.SortingBuilder;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
    Collection collection = new Collection();
    collection.setCreated(LocalDateTime.parse("2020-03-03T16:12:08.686626"));
    LocalizedText genericLabel = new LocalizedText();
    genericLabel.setText(
        Locale.GERMAN,
        "100(0) Schlüsseldokumente - zur deutschen Geschichte im 20. Jahrhundert sowie zur russischen und sowjetischen Geschichte (1917-1991)");
    genericLabel.setText(
        Locale.ENGLISH,
        "100(0) Key Documents on German History of the 20th Century and of the Russian and Soviet History (1917-1991)");
    collection.setLabel(genericLabel);
    collection.setLastModified(LocalDateTime.parse("2020-10-19T17:04:07.889254"));
    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setUuid(UUID.fromString("cc3893e8-9530-4602-b640-118a3218e826"));
    previewImage.setFilename("1000_Schluesseldokumente_neutrales_Logo.jpg");
    previewImage.setHttpBaseUrl(
        new URL(
            "https://api.digitale-sammlungen.de/iiif/image/v2/cc3893e8-9530-4602-b640-118a3218e826"));
    previewImage.setUri(
        new URI("file:///cudami/image/jpg/cc38/93e8/9530/4602/b640/118a/3218/e826/resource.jpg"));
    previewImage.setMimeType(MimeType.MIME_IMAGE_JPEG);
    collection.setPreviewImage(previewImage);
    RenderingHintsPreviewImage previewImageRenderingHints = new RenderingHintsPreviewImage();
    previewImageRenderingHints.setAltText(genericLabel);
    previewImageRenderingHints.setTitle(genericLabel);
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    collection.setPreviewImageRenderingHints(previewImageRenderingHints);
    collection.setUuid(UUID.fromString("0b0b89e1-3f8a-4928-b8f3-67a8c4b3ff57"));
    collection.setRefId(14);
    collection.setPublicationStart(LocalDate.parse("2020-10-01"));

    PageResponse<Collection> response = new PageResponse();
    response.setTotalElements(139);
    response.setContent(List.of(collection));
    Order order1 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("de")
            .build();
    Order order2 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("")
            .build();
    Sorting sorting = new SortingBuilder().order(order1).order(order2).build();
    PageRequest pageRequest = new PageRequestBuilder().pageSize(1).sorting(sorting).build();
    response.setPageRequest(pageRequest);

    when(collectionService.find(any(PageRequest.class))).thenReturn(response);
    when(collectionService.findActive(any(PageRequest.class))).thenReturn(response);

    testJson(path, "/v2/collections/collectionlist.json");
  }

  @DisplayName("shall filter out inactive collections from the collection list")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/collections/?pageNumber=0&pageSize=1&active=true"})
  public void collectionListForInactive(String path) throws Exception {
    PageResponse<Collection> response = new PageResponse();
    response.setTotalElements(0);
    response.setContent(List.of());
    Order order1 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("de")
            .build();
    Order order2 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("")
            .build();
    Sorting sorting = new SortingBuilder().order(order1).order(order2).build();
    PageRequest pageRequest = new PageRequestBuilder().pageSize(1).sorting(sorting).build();
    response.setPageRequest(pageRequest);

    when(collectionService.findActive(any(PageRequest.class))).thenReturn(response);
    testJson(path, "/v2/collections/collectionlist_inactive.json");
  }
}
