package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ItemController.class)
@DisplayName("The ItemController")
class ItemControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;
  @MockBean private ItemService itemService;

  @DisplayName("can retrieve an item by identifier without any special characters")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/items/identifier/foo:bar", "/v6/items/identifier/foo:bar.json"})
  public void getByIdentifierWithoutSpecialCharacters(String path) throws Exception {
    when(itemService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(Item.builder().build());

    ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> identifierCaptor = ArgumentCaptor.forClass(String.class);

    testHttpGet(path);

    verify(itemService, times(1))
        .getByIdentifier(namespaceCaptor.capture(), identifierCaptor.capture());

    assertThat(namespaceCaptor.getValue()).isEqualTo("foo");
    assertThat(identifierCaptor.getValue()).isEqualTo("bar");
  }

  @DisplayName("can retrieve an item by identifier with unencoded slashes as identifier")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v6/items/identifier/foo:bar/baz", "/v6/items/identifier/foo:bar/baz.json"})
  public void getByIdentifierWithUnencodedSlashesAsIdentifier(String path) throws Exception {
    when(itemService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(Item.builder().build());

    ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> identifierCaptor = ArgumentCaptor.forClass(String.class);

    testHttpGet(path);

    verify(itemService, times(1))
        .getByIdentifier(namespaceCaptor.capture(), identifierCaptor.capture());

    assertThat(namespaceCaptor.getValue()).isEqualTo("foo");
    assertThat(identifierCaptor.getValue()).isEqualTo("bar/baz");
  }

  @DisplayName("can retrieve an item by identifier with Base64 encoded slashes as identifier")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/items/identifier/",
      })
  public void getByIdentifierWithBase64EncodedSlashesAsIdentifier() throws Exception {
    String path =
        "/v6/items/identifier/"
            + Base64.encodeBase64String("foo:bar/baz".getBytes(StandardCharsets.UTF_8));

    when(itemService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(Item.builder().build());

    ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> identifierCaptor = ArgumentCaptor.forClass(String.class);

    testHttpGet(path);

    verify(itemService, times(1))
        .getByIdentifier(namespaceCaptor.capture(), identifierCaptor.capture());

    assertThat(namespaceCaptor.getValue()).isEqualTo("foo");
    assertThat(identifierCaptor.getValue()).isEqualTo("bar/baz");
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/items/identifier/foo:bar",
        "/v6/items/identifier/foo:bar.json",
        "/v5/items/identifier/foo:bar",
        "/v5/items/identifier/foo:bar.json",
        "/v2/items/identifier/foo:bar",
        "/v2/items/identifier/foo:bar.json",
        "/latest/items/identifier/foo:bar",
        "/latest/items/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Item expected = Item.builder().build();

    when(itemService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(itemService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/items/identifier/",
        "/v5/items/identifier/",
        "/v2/items/identifier/",
        "/latest/items/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Item expected = Item.builder().build();

    when(itemService.getByIdentifier(eq("foo"), eq("bar/bla"))).thenReturn(expected);

    testHttpGet(
        basePath
            + java.util.Base64.getEncoder()
                .encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(itemService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }

  @DisplayName("can filter items by the uuid of their \"parent\" item")
  @Test
  public void filterByPartOfItemUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    testHttpGet("/v6/items?pageNumber=0&pageSize=100&part_of_item.uuid=eq:" + uuid);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(100)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("part_of_item.uuid")
                            .isEquals(uuid)
                            .build())
                    .build())
            .build();
    verify(itemService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can return a paged list of DigitalObjects for an item, even it the item")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/items/1c72ae9a-94e1-45b1-848f-da1303000924/digitalobjects?pageSize=25&pageNumber=0"
      })
  public void pageListOfDigitalObjectsForItem(String path) throws Exception {
    DigitalObject expectedDigitalObject =
        DigitalObject.builder()
            .uuid(UUID.fromString("604ed5f3-d245-4829-b8ad-297cc947af7e"))
            .label("Hello")
            .build();
    UUID itemUuid = UUID.fromString("1c72ae9a-94e1-45b1-848f-da1303000924");
    PageResponse<DigitalObject> expectedPageResponse =
        PageResponse.builder()
            .forPageSize(25)
            .forRequestPage(0)
            .withTotalElements(1)
            .withContent(List.of(expectedDigitalObject))
            .build();
    when(itemService.findDigitalObjects(eq(itemUuid), any(PageRequest.class)))
        .thenReturn(expectedPageResponse);

    testJson(path, "/v6/items/1c72ae9a-94e1-45b1-848f-da1303000924_digitalobjects.json");
  }
}
