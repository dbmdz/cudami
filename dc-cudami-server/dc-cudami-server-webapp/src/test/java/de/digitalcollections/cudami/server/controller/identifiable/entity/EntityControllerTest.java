package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.CollectionBuilder;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Entity;
import java.util.Locale;
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

    Entity expected =
        new CollectionBuilder()
            .withUuid("7ff8a9ab-d596-4e5d-a8e1-7956d553293b")
            .createdAt("2020-07-13T11:14:13.397483")
            .withLabel(Locale.GERMAN, "Perikopenbuch Heinrichs II.")
            .withLabel(Locale.ENGLISH, "Evangeliary of Henry II")
            .lastModifiedAt("2020-11-20T11:55:17.862953")
            .withPreviewImage(
                "default.jpg",
                "b6496877-4957-45a1-9d4c-d0c53b9ccd3d",
                "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00087481_00007/202,159,4023,6252/200,/0/default.jpg",
                MimeType.MIME_IMAGE_PNG)
            .withOpenPreviewImageInNewWindow()
            .withRefId(123)
            .build();

    when(entityService.getByRefId(anyLong())).thenReturn(expected);

    testJson(path);
  }
}
