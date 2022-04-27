package de.digitalcollections.cudami.server.controller.identifiable.entity.relation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3EntityRelationController.class)
@DisplayName("The V3 EntityRelationController")
public class V3EntityRelationControllerTest extends BaseControllerTest {

  @MockBean private EntityRelationService entityRelationService;

  @DisplayName("shall return a list of related entities with a predicate")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/entities/relations?predicate=is_part_of&pageNumber=0&pageSize=1"})
  public void listOfRelatedEntities(String path) throws Exception {
    PageResponse<EntityRelation> expected =
        (PageResponse<EntityRelation>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .forEqualPredicate("predicate", "is_part_of")
                .withTotalElements(109)
                .withContent(
                    EntityRelation.builder()
                        .withObject(
                            Collection.builder()
                                .createdAt("2020-08-12T18:14:26.032971")
                                .withLabel(Locale.GERMAN, "Test-Archiv")
                                .lastModifiedAt("2020-10-19T16:45:18.55684")
                                .withPreviewImage(
                                    "Test.jpg",
                                    "c0b2db2a-b3eb-47fb-b8f9-e9a7bb158e39",
                                    "file:///cudami/image/jpg/c0b2/db2a/b3eb/47fb/b8f9/e9a7/bb15/8e39/resource.jpg",
                                    MimeType.MIME_IMAGE_JPEG,
                                    "https://api.digitale-sammlungen.de/iiif/image/v2/c0b2db2a-b3eb-47fb-b8f9-e9a7bb158e39")
                                .withAltText(Locale.GERMAN, "Test")
                                .withTitle(Locale.GERMAN, "Test")
                                .withOpenPreviewImageInNewWindow()
                                .withUuid("21d24b9e-7a39-4c79-83c8-7d70ef260a93")
                                .withRefId(249)
                                .build())
                        .withPredicate("is_part_of")
                        .withSubject(
                            Project.builder()
                                .createdAt("2020-09-30T16:25:10.901715")
                                .withIdentifier(
                                    "mdz-proj",
                                    "1496151210",
                                    "9ded8827-d4bd-4ddc-9c4e-9ebd74b795c0")
                                .withLabel(Locale.GERMAN, "Parent-Archiv")
                                .lastModifiedAt("2021-04-16T04:15:01.490064")
                                .withUuid("34c36d4b-ee84-4eb3-9698-a14d22079d99")
                                .withRefId(1300653)
                                .build())
                        .build())
                .build();

    when(entityRelationService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v3/entities/relations.json");
  }
}
