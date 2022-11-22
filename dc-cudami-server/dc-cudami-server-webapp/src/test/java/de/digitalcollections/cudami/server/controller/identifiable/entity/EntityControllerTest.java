package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Project;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            .uuid("7ff8a9ab-d596-4e5d-a8e1-7956d553293b")
            .created("2020-07-13T11:14:13.397483")
            .label(Locale.GERMAN, "Perikopenbuch Heinrichs II.")
            .label(Locale.ENGLISH, "Evangeliary of Henry II")
            .lastModified("2020-11-20T11:55:17.862953")
            .previewImage(
                "default.jpg",
                "b6496877-4957-45a1-9d4c-d0c53b9ccd3d",
                "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00087481_00007/202,159,4023,6252/200,/0/default.jpg",
                MimeType.MIME_IMAGE_PNG)
            .openPreviewImageInNewWindow()
            .refId(123)
            .build();

    when(entityService.getByRefId(anyLong())).thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns an entity, e.g. a project, by its identifier")
  @ParameterizedTest
  @ValueSource(strings = {"/latest/entities/identifier/mdz-proj:1328176523.json"})
  public void returnByIdentifier(String path) throws IdentifiableServiceException {
    Entity expected =
        Project.builder()
            .created("2020-09-30T16:25:09.365401")
            .identifier("mdz-proj", "1328176523", "54edc946-5e10-495a-bda2-cdc2ebd4e3d6")
            .label(
                Locale.GERMAN,
                "Notendrucke des 16. und 17. Jahrhunderts mit mehrstimmiger Musik in der BSB")
            .label(
                Locale.ENGLISH,
                "Printed sources of polyphonic music (1501-1700) from the Bavarian State Library: digitization and online presentation")
            .lastModified("2021-04-16T04:15:00.961914")
            .uuid("d76bb26f-bd7d-4ade-be48-a2139d49dbf1")
            .refId(1300518)
            .build();
    when(entityService.getByIdentifier(eq("mdz-proj"), eq("1328176523"))).thenReturn(expected);
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/entities/identifier/foo:bar",
        "/v5/entities/identifier/foo:bar",
        "/latest/entities/identifier/foo:bar",
        "/v6/entities/identifier/foo:bar.json",
        "/v5/entities/identifier/foo:bar.json",
        "/latest/entities/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Entity expected = Entity.builder().build();

    when(entityService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(entityService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/entities/identifier/", "/v5/entities/identifier/"})
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Entity expected = Entity.builder().build();

    when(entityService.getByIdentifier(eq("foo"), eq("bar/bla"))).thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(entityService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }
}
