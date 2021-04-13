package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(EntityController.class)
@DisplayName("The EntityController")
class EntityControllerTest extends BaseControllerTest {

  @MockBean
  @Qualifier("entityService")
  private EntityService<Entity> entityService;

  @MockBean private EntityRelationService entityRelationService;

  @DisplayName("returns an entity, e.g. a collection, by its refid")
  @ParameterizedTest
  @ValueSource(strings = {"/latest/entities/123"})
  public void returnByRefid(String path) throws Exception {
    Entity entity = new Entity();
    entity.setCreated(LocalDateTime.parse("2020-07-13T11:14:13.397483"));
    LocalizedText label = new LocalizedText();
    label.setText(Locale.GERMAN, "Perikopenbuch Heinrichs II.");
    label.setText(Locale.ENGLISH, "Evangeliary of Henry II");
    entity.setLabel(label);
    entity.setLastModified(LocalDateTime.parse("2020-11-20T11:55:17.862953"));

    ImageFileResource previewImage = new ImageFileResource();
    previewImage.setUuid(UUID.fromString("b6496877-4957-45a1-9d4c-d0c53b9ccd3d"));
    previewImage.setType(IdentifiableType.RESOURCE);
    previewImage.setFilename("default.jpg");
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setUri(
        new URI(
            "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00087481_00007/202,159,4023,6252/200,/0/default.jpg"));
    previewImage.setMimeType(MimeType.MIME_IMAGE_PNG);
    previewImage.setReadonly(false);
    previewImage.setSizeInBytes(0);
    previewImage.setHeight(0);
    previewImage.setWidth(0);
    entity.setPreviewImage(previewImage);

    RenderingHintsPreviewImage previewImageRenderingHints = new RenderingHintsPreviewImage();
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    entity.setPreviewImageRenderingHints(previewImageRenderingHints);

    entity.setType(IdentifiableType.ENTITY);
    entity.setUuid(UUID.fromString("7ff8a9ab-d596-4e5d-a8e1-7956d553293b"));
    entity.setEntityType(EntityType.COLLECTION);
    entity.setRefId(123);

    when(entityService.getByRefId(anyLong())).thenReturn(entity);

    testJson(path);
  }
}
