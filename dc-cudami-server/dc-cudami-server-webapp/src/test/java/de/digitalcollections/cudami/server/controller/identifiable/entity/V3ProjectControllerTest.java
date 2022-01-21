package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3ProjectController.class)
@DisplayName("The V3 ProjectController")
public class V3ProjectControllerTest extends BaseControllerTest {

  @MockBean private ProjectService projectService;

  @DisplayName("shall return a paged list of digital objects, which belong to a project")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/projects/d0e3ce0f-f030-4c7f-8f78-5606173f1a11/digitalobjects?pageNumber=0&pageSize=1"
      })
  public void getDigitalObjectsForProject(String path) throws Exception {
    SearchPageResponse<DigitalObject> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder()
                .forPageSize(1)
                .forRequestPage(0)
                .withTotalElements(45)
                .withContent(
                    new DigitalObjectBuilder()
                        .createdAt("2020-09-29T12:18:04.059448")
                        .withIdentifier(
                            "mdz-obj", "bsb00057492", "68a7fbc6-52de-462e-a2a2-913f9d4c9aca")
                        .withLabel(
                            "Stammens-Beschreibung des des Hainhoferischen Geschlechts - SuStB Augsburg 2°Cod Aug 14")
                        .lastModifiedAt("2020-09-29T12:18:04.05945")
                        .withPreviewImage(
                            "default.jpg",
                            "0f8a4d25-7a7e-44e1-9dcd-7db1afa85ee1",
                            "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00057492_00005/full/250,/0/default.jpg",
                            MimeType.MIME_IMAGE,
                            "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00057492_00005")
                        .withUuid("b7aadcd5-02cb-4e6a-897b-11710e706e55")
                        .withRefId(52867)
                        .build())
                .build();

    when(projectService.getDigitalObjects(any(Project.class), any(SearchPageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }
}
