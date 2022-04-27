package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Project;
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
        Collection.builder()
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

  @DisplayName("returns an entity, e.g. a project, by its identifier")
  @ParameterizedTest
  @ValueSource(strings = {"/latest/entities/identifier/mdz-proj:1328176523.json"})
  public void returnByIdentifier(String path) {
    Entity expected =
        Project.builder()
            .createdAt("2020-09-30T16:25:09.365401")
            .withIdentifier("mdz-proj", "1328176523", "54edc946-5e10-495a-bda2-cdc2ebd4e3d6")
            .withLabel(
                Locale.GERMAN,
                "Notendrucke des 16. und 17. Jahrhunderts mit mehrstimmiger Musik in der BSB")
            .withLabel(
                Locale.ENGLISH,
                "Printed sources of polyphonic music (1501-1700) from the Bavarian State Library: digitization and online presentation")
            .lastModifiedAt("2021-04-16T04:15:00.961914")
            .withUuid("d76bb26f-bd7d-4ade-be48-a2139d49dbf1")
            .withRefId(1300518)
            .build();
    when(entityService.getByIdentifier(eq("mdz-proj"), eq("1328176523"))).thenReturn(expected);
  }
}
