package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(extractFirstUuidFromPath(path));
    PageResponse<Collection> emptyPageResponse = new PageResponse<>();
    PageRequest pageRequest = new PageRequest();
    Filtering filtering = new Filtering();
    FilterCriterion filterCriterionStart =
        new FilterCriterion(
            "c.publication_start",
            FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET,
            LocalDate.parse("2021-03-31"));
    FilterCriterion filterCriterionEnd =
        new FilterCriterion(
            "c.publication_end",
            FilterOperation.GREATER_THAN_OR_NOT_SET,
            LocalDate.parse("2021-03-31"));
    filtering.setFilterCriteria(List.of(filterCriterionStart, filterCriterionEnd));
    pageRequest.setFiltering(filtering);
    pageRequest.setPageNumber(0);
    pageRequest.setPageSize(1000);
    emptyPageResponse.setPageRequest(pageRequest);

    when(digitalObjectService.getActiveCollections(eq(digitalObject), any(PageRequest.class)))
        .thenReturn(emptyPageResponse);

    testJson(path);
  }

  @DisplayName("returns a digital object by its identifier")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/digitalobjects/identifier/mdz-obj:bsb10000001"})
  public void getDigitalObjectByIdentifier(String path) throws Exception {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setCreated(LocalDateTime.parse("2020-08-21T07:49:37.004443"));
    Identifier identifier =
        new Identifier(
            UUID.fromString("1c419226-8d61-4efa-923a-7fbaf961eb9d"), "mdz-obj", "bsb10000001");
    identifier.setUuid(UUID.fromString("53e3e619-47a3-4110-84f7-acba12a52298"));
    digitalObject.addIdentifier(identifier);
    digitalObject.setLabel(
        "Actorum Bohemicorum, ... Theil, Das ist: Warhaffte vnd eigentliche Beschreibung aller fürnembsten vnd denckwürdigsten Historien vnd Geschichten, Welche sich im Königreich Böheim vnd dessen incorporirten Ländern ... begeben vnd zugetragen haben : Auß allerhand glaubwürdigen Publicis scriptis in eine feine richtige Ordnung zusammen verfasset, jetzo mit fleiß ubersehen, gemehret vnd auffs newe zugerichtet");
    digitalObject.setLastModified(LocalDateTime.parse("2020-08-21T07:49:37.00445"));
    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setFilename("default.jpg");
    previewImage.setUuid(UUID.fromString("abe16b03-c5d5-41a6-9475-f742e06ae881"));
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setMimeType(MimeType.MIME_IMAGE);
    previewImage.setUri(
        URI.create(
            "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg"));
    digitalObject.setPreviewImage(previewImage);
    digitalObject.setUuid(UUID.fromString("1c419226-8d61-4efa-923a-7fbaf961eb9d"));
    digitalObject.setEntityType(EntityType.DIGITAL_OBJECT);
    digitalObject.setRefId(72);

    when(digitalObjectService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(digitalObject);

    testJson(path);
  }
}
