package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
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
  // @Disabled("requires Re-Mapping of the digitalObject class name")
  public void digitalObjectsForCollection(String path) throws Exception {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setCreated(LocalDateTime.parse("2020-09-29T10:58:30.458925"));
    Identifier mdzObjIdentifier =
        new Identifier(
            UUID.fromString("66cdabfc-5b15-44f5-a01e-41aa8be2b9e2"), "mdz-obj", "bsb00000610");
    mdzObjIdentifier.setUuid(UUID.fromString("5a4c1a74-40c9-4175-8f25-e5267eddaabc"));
    digitalObject.addIdentifier(mdzObjIdentifier);
    digitalObject.setLabel(
        "Die neuesten Schlager aus: Wenn Liebe erwacht : Hollandweibchen, Liebe im Schnee, Strohwitwe ... und viele andere ; Schlager u. Modelieder zum Mitsingen");
    digitalObject.setLastModified(LocalDateTime.parse("2020-09-29T10:58:30.458928"));
    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setUuid(UUID.fromString("94091a4d-c3ce-448c-93d1-7e86d8c3448a"));
    previewImage.setType(IdentifiableType.RESOURCE);
    previewImage.setFilename("/iiif/image/v2/bsb00000610_00002/full/250,/0/default.jpg");
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setMimeType(MimeType.MIME_IMAGE);
    previewImage.setReadonly(false);
    previewImage.setSizeInBytes(0);
    previewImage.setUri(
        URI.create(
            "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00000610_00002/full/250,/0/default.jpg"));
    previewImage.setHeight(0);
    previewImage.setWidth(0);
    digitalObject.setPreviewImage(previewImage);
    digitalObject.setType(IdentifiableType.ENTITY);
    digitalObject.setUuid(UUID.fromString("66cdabfc-5b15-44f5-a01e-41aa8be2b9e2"));
    digitalObject.setEntityType(EntityType.DIGITAL_OBJECT);
    digitalObject.setRefId(441);

    Collection collection = new Collection();
    collection.setUuid(extractFirstUuidFromPath(path));

    SearchPageResponse<DigitalObject> response = new SearchPageResponse();
    response.setContent(List.of(digitalObject));
    PageRequest pageRequest = new PageRequestBuilder().pageSize(1).build();
    response.setPageRequest(pageRequest);
    response.setTotalElements(319);

    when(collectionService.getDigitalObjects(eq(collection), any(SearchPageRequest.class)))
        .thenReturn(response);

    testJson(path);
  }

  @DisplayName("shall return subcollections")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/collections/a6193be6-1048-4c86-be54-7a99dbbb586c/subcollections?active=true&pageNumber=0&pageSize=1"
      })
  public void subcollections(String path) throws Exception {
    Collection collection = new Collection();
    collection.setCreated(LocalDateTime.parse("2020-07-10T12:12:33.099312"));
    LocalizedStructuredContent description = new LocalizedStructuredContent();
    StructuredContent descriptionParagraphDe = new StructuredContent();
    descriptionParagraphDe.addContentBlock(
        new Paragraph(
            "Mittelalterliche und neuzeitliche Handschriften aus aller Welt, Briefe und Autographen, Musikhandschriften"));
    description.put(Locale.GERMAN, descriptionParagraphDe);
    StructuredContent descriptionParagraphEn = new StructuredContent();
    descriptionParagraphEn.addContentBlock(
        new Paragraph(
            "Medieval and modern manuscripts from all over the world, letters and autographs, music manuscripts"));
    description.put(Locale.ENGLISH, descriptionParagraphEn);
    collection.setDescription(description);
    LocalizedText label = new LocalizedText();
    label.put(Locale.GERMAN, "Handschriften");
    label.put(Locale.ENGLISH, "Manuscripts");
    collection.setLabel(label);
    collection.setLastModified(LocalDateTime.parse("2020-11-05T17:00:27.181566"));
    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setUuid(UUID.fromString("fb167832-e1d2-4729-a3b9-f68af5fca0c3"));
    previewImage.setType(IdentifiableType.RESOURCE);
    previewImage.setFilename("Hauptsammlung_Handschriften.jpg");
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setHttpBaseUrl(
        new URL(
            "https://api.digitale-sammlungen.de/iiif/image/v2/fb167832-e1d2-4729-a3b9-f68af5fca0c3"));
    previewImage.setMimeType(MimeType.MIME_IMAGE_JPEG);
    previewImage.setReadonly(false);
    previewImage.setSizeInBytes(0);
    previewImage.setUri(
        new URI("file:///cudami/image/jpg/fb16/7832/e1d2/4729/a3b9/f68a/f5fc/a0c3/resource.jpg"));
    previewImage.setHeight(0);
    previewImage.setWidth(0);
    collection.setPreviewImage(previewImage);
    RenderingHintsPreviewImage previewImageRenderingHints = new RenderingHintsPreviewImage();
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    collection.setPreviewImageRenderingHints(previewImageRenderingHints);
    collection.setType(IdentifiableType.ENTITY);
    collection.setUuid(UUID.fromString("888db95f-f837-4b17-bd3c-c00fd8c5205c"));
    collection.setEntityType(EntityType.COLLECTION);
    collection.setRefId(115);
    collection.setPublicationStart(LocalDate.parse("2020-10-01"));

    PageResponse<Collection> response = new PageResponse();
    response.setContent(List.of(collection));
    SearchPageRequest pageRequest = new SearchPageRequest();
    pageRequest.setPageNumber(0);
    pageRequest.setPageSize(1);
    response.setTotalElements(8);
    Filtering filtering = new Filtering();
    FilterCriterion filterCriterionStart =
        new FilterCriterion(
            "publicationStart",
            FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET,
            LocalDate.parse("2021-04-12"));
    FilterCriterion filterCriterionEnd =
        new FilterCriterion(
            "publicationEnd",
            FilterOperation.GREATER_THAN_OR_NOT_SET,
            LocalDate.parse("2021-04-12"));
    filtering.setFilterCriteria(List.of(filterCriterionStart, filterCriterionEnd));
    pageRequest.setFiltering(filtering);
    response.setPageRequest(pageRequest);

    when(collectionService.getActiveChildren(any(UUID.class), any(PageRequest.class)))
        .thenReturn(response);

    testJson(path);
  }
}
