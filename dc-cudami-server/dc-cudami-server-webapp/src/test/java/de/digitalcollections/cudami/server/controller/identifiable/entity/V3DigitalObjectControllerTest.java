package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.filter.FilteringBuilder;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
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

@WebMvcTest(V3DigitalObjectController.class)
@DisplayName("The DigitalObjectController V3")
public class V3DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean
  private DigitalObjectService digitalObjectService;

  @DisplayName("can return the collections, a digital object belongs to")
  @ParameterizedTest
  @ValueSource(
      strings = {
          "/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7/collections?active=true&pageNumber=0&pageSize=1"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {
    PageResponse<Collection> pageResponse = new PageResponse<>();

    Collection collection = new Collection();
    collection.setCreated(LocalDateTime.parse("2020-07-16T11:51:33.981829"));
    LocalizedText label = new LocalizedText();
    label.setText(Locale.GERMAN,"Lateinische Handschriften");
    label.setText(Locale.ENGLISH,"Latin Manuscripts");
    collection.setLabel(label);
    collection.setLastModified(LocalDateTime.parse("2020-11-04T15:46:42.81741"));
    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setType(IdentifiableType.RESOURCE);
    previewImage.setUuid(UUID.fromString("2780bee1-eeec-4b50-a95b-bba90793fc6a"));
    previewImage.setFilename("Lateinische_Handschriften_bsb00131281_27.jpg");
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setHttpBaseUrl(new URL("https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a"));
    previewImage.setMimeType(MimeType.MIME_IMAGE_JPEG);
    previewImage.setUri(new URI("file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg"));
    collection.setPreviewImage(previewImage);
    RenderingHintsPreviewImage previewImageRenderingHints = new RenderingHintsPreviewImage();
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    collection.setPreviewImageRenderingHints(previewImageRenderingHints);
    collection.setType(IdentifiableType.ENTITY);
    collection.setUuid(UUID.fromString("25198d8b-38d4-49f7-9ef0-d99b3e607e30"));
    collection.setEntityType(EntityType.COLLECTION);
    collection.setRefId(148);
    collection.setPublicationStart(LocalDate.parse("2020-11-01"));
    pageResponse.setContent(List.of(collection));
    Filtering filtering = new FilteringBuilder()
        .add(new FilterCriterion("c.publication_start", FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET,
            LocalDate.parse("2021-04-12")))
        .add(new FilterCriterion("c.publication_end", FilterOperation.GREATER_THAN_OR_NOT_SET,
            LocalDate.parse("2021-04-12")))
        .build();
    PageRequest pageRequest = new PageRequestBuilder().pageNumber(0).pageSize(1).filtering(filtering).build();
    pageResponse.setPageRequest(pageRequest);
    pageResponse.setTotalElements(1);

    when(digitalObjectService.getActiveCollections(any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(pageResponse);

    testJson(path);
  }

}
