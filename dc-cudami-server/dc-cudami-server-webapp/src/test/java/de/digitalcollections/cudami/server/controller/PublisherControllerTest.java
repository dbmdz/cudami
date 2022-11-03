package de.digitalcollections.cudami.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.PublisherService;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(PublisherController.class)
@DisplayName("The PublisherController")
class PublisherControllerTest extends BaseControllerTest {

  @MockBean private PublisherService publisherService;

  @DisplayName("can persist a newly created publisher")
  @Test
  public void save() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    Publisher publisher = Publisher.builder().publisherPresentation("foo").build();
    Publisher persistedPublisher =
        Publisher.builder().uuid(uuid).publisherPresentation("foo").build();
    when(publisherService.save(any(Publisher.class))).thenReturn(persistedPublisher);

    String body =
        "{\n"
            + "  \"objectType\": \"PUBLISHER\",\n"
            + "  \"publisherPresentation\": \"foo\"\n"
            + "}";

    testPostJson(
        "/v6/publishers", body, "/v6/publishers/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName("throws an error when trying to update a publisher without uuid")
  @Test
  public void updateWithoutUuid() throws Exception {
    String body =
        "{\n"
            + "  \"objectType\": \"PUBLISHER\",\n"
            + "  \"publisherPresentation\": \"foo\"\n"
            + "}";

    testPutJsonWithState("/v6/publishers/12345678-1234-1234-1234-123456789012", body, 500);
  }

  @DisplayName("can update an existing publisher")
  @Test
  public void update() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    Publisher publisher = Publisher.builder().publisherPresentation("foo").uuid(uuid).build();
    when(publisherService.update(any(Publisher.class))).thenReturn(publisher);

    String body =
        "{\n"
            + "  \"objectType\": \"PUBLISHER\",\n"
            + "  \"uuid\": \"12345678-1234-1234-1234-123456789012\",\n"
            + "  \"publisherPresentation\": \"foo\"\n"
            + "}";

    testPutJson(
        "/v6/publishers/12345678-1234-1234-1234-123456789012",
        body,
        "/v6/publishers/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName("returns a 404 state, when get by uuid returns no publisher")
  @Test
  public void getByUuidFail() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    when(publisherService.getByUuid(eq(uuid))).thenReturn(null);
    testHttpGetWithExpectedStatus("/v6/publishers/12345678-1234-1234-1234-123456789012", 404);
  }

  @DisplayName("can return a Publisher by its uuid")
  @Test
  public void getByUuid() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    Publisher publisher = Publisher.builder().publisherPresentation("foo").uuid(uuid).build();
    when(publisherService.getByUuid(eq(uuid))).thenReturn(publisher);
    testJson("/v6/publishers/" + uuid, "/v6/publishers/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName(
      "returns a 404 state, when delete by uuid fails (because the resource did not exist)")
  @Test
  public void deleteFail() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    when(publisherService.delete(eq(uuid))).thenReturn(false);
    testDeleteNotFound("/v6/publishers/12345678-1234-1234-1234-123456789012");
  }

  @DisplayName("can delete a publisher")
  @Test
  public void delete() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    when(publisherService.delete(eq(uuid))).thenReturn(true);
    testDeleteSuccessful("/v6/publishers/12345678-1234-1234-1234-123456789012");
  }

  @DisplayName("can page publishers")
  @Test
  public void page() throws Exception {
    UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
    Publisher publisher = Publisher.builder().publisherPresentation("foo").uuid(uuid).build();

    PageResponse pageResponse =
        PageResponse.builder()
            .forPageSize(1)
            .forRequestPage(0)
            .withContent(List.of(publisher))
            .build();
    when(publisherService.find(any(PageRequest.class))).thenReturn(pageResponse);

    testJson("/v6/publishers?pageSize=1&pageNumber=0", "/v6/publishers/list.json");
  }

  @DisplayName("can filter publishers with agent and one location")
  @Test
  public void filterOneAgentOneLocation() throws Exception {
    UUID agentUuid = UUID.fromString("23456789-2345-2345-2345-234567890123");
    UUID locationUuid = UUID.randomUUID();

    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(1)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("agent_uuid")
                            .isEquals(agentUuid)
                            .build())
                    .add(
                        FilterCriterion.builder()
                            .withExpression("location_uuids")
                            .isEquals(List.of(locationUuid))
                            .build())
                    .build())
            .build();

    testHttpGetWithExpectedStatus(
        "/v6/publishers?pageSize=1&pageNumber=0&agent_uuid=eq:"
            + agentUuid
            + "&location_uuids=eq:"
            + locationUuid,
        200);
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    verify(publisherService, times(1)).find(pageRequestArgumentCaptor.capture());

    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }

  @DisplayName("can filter publishers with agent and multiple locations")
  @Test
  public void filterOneAgentMultipleLocations() throws Exception {
    UUID agentUuid = UUID.fromString("23456789-2345-2345-2345-234567890123");
    UUID locationUuid1 = UUID.randomUUID();
    UUID locationUuid2 = UUID.randomUUID();

    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(1)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("agent_uuid")
                            .isEquals(agentUuid)
                            .build())
                    .add(
                        FilterCriterion.builder()
                            .withExpression("location_uuids")
                            .isEquals(List.of(locationUuid1, locationUuid2))
                            .build())
                    .build())
            .build();

    testHttpGetWithExpectedStatus(
        "/v6/publishers?pageSize=1&pageNumber=0&agent_uuid=eq:"
            + agentUuid
            + "&location_uuids=eq:"
            + locationUuid1
            + ","
            + locationUuid2,
        200);
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);
    verify(publisherService, times(1)).find(pageRequestArgumentCaptor.capture());

    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }
}
