package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(CollectionController.class)
@DisplayName("The CollectionController")
class CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;

  // TODO: Test latest/collections/<uuid>/subcollections with and without active flag
  @DisplayName("shall return digital objects for a collection")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/collections/a014a33b-6803-4b17-a876-a8f68758f2a7/digitalobjects?pageNumber=0&pageSize=1"
      })
  @Disabled("requires Re-Mapping of the digitalObject class name")
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
    when(collectionService.getDigitalObjects(eq(collection), any(SearchPageRequest.class)))
        .thenReturn(response);

    testJson(path);
  }
}
