package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5TopicController.class)
@DisplayName("The V5 Topic Controller")
class V5TopicControllerTest extends BaseControllerTest {

  @MockBean private TopicService topicService;

  @MockBean private LocaleService localeService;

  @DisplayName("shall return a paged list of topic")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/topics?pageSize=1&pageNumber=0",
        "/v2/topics?pageSize=1&pageNumber=0",
        "/latest/topics?pageSize=1&pageNumber=0"
      })
  void testFind(String path) throws Exception {
    PageResponse<Topic> expected =
        (PageResponse<Topic>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(2)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        Topic.builder()
                            .label(Locale.GERMAN, "Mein erster Inhaltsbaum")
                            .created("2019-02-14T00:00:00")
                            .lastModified("2019-02-14T00:00:00")
                            .uuid("2a996663-5faa-46ac-883a-97b52f38b493")
                            .refId(28)
                            .build()))
                .build();

    when(topicService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/topics/find_with_result.json");
  }

  @DisplayName("shall return a paged list of entities of a topic")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/topics/06b8a104-e052-4104-b20a-502866de4deb/entities",
        "/v3/topics/06b8a104-e052-4104-b20a-502866de4deb/entities",
        "/latest/topics/06b8a104-e052-4104-b20a-502866de4deb/entities"
      })
  void testFindEntities(String path) throws Exception {
    PageResponse<Entity> expected =
        (PageResponse<Entity>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withoutContent()
                .build();

    when(topicService.findEntities(any(UUID.class), any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/topics/find_with_empty_result.json");
  }

  @DisplayName("shall return a paged list of subtopics of a topic")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/topics/06b8a104-e052-4104-b20a-502866de4deb/subtopics"})
  void testFindSobtopics(String path) throws Exception {
    PageResponse<Topic> expected =
        (PageResponse<Topic>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        Topic.builder()
                            .label(Locale.GERMAN, "Ortsknoten")
                            .created("2019-02-14T00:00:00")
                            .lastModified("2019-02-14T00:00:00")
                            .uuid("06b8a104-e052-4104-b20a-502866de4deb")
                            .refId(1346230)
                            .build()))
                .build();

    when(topicService.findSubParts(any(UUID.class), any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/topics/06b8a104-e052-4104-b20a-502866de4deb_subtopics.json");
  }

  @DisplayName("shall return a paged list of file resources of a topic")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/topics/06b8a104-e052-4104-b20a-502866de4deb/fileresources",
        "/v3/topics/06b8a104-e052-4104-b20a-502866de4deb/fileresources",
        "/latest/topics/06b8a104-e052-4104-b20a-502866de4deb/fileresources"
      })
  void testFindFileresources(String path) throws Exception {
    PageResponse<FileResource> expected =
        (PageResponse<FileResource>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withoutContent()
                .build();

    when(topicService.findFileResources(any(UUID.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/topics/find_with_empty_result.json");
  }

  @DisplayName("shall return a paged list of top topics ")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/topics/top", "/v3/topics/top", "/latest/topics/top"})
  void testFindTopTopics(String path) throws Exception {
    PageResponse<Topic> expected =
        (PageResponse<Topic>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        Topic.builder()
                            .label(Locale.GERMAN, "Mein erster Inhaltsbaum")
                            .created("2019-02-14T00:00:00")
                            .lastModified("2019-02-14T00:00:00")
                            .uuid("2a996663-5faa-46ac-883a-97b52f38b493")
                            .refId(28)
                            .build()))
                .build();

    when(topicService.findRootNodes(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/topics/top.json");
  }
}
