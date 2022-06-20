package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.work.Item;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ItemController.class)
@DisplayName("The ItemController")
class ItemControllerTest extends BaseControllerTest {

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

  @DisplayName(
      "throws an error, when the base64 encoded identifier is invalid and thus is treated as an unencoded identifier")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/items/identifier/",
      })
  public void getByIdentifierWithInvalidEncodedSlashesAsIdentifier() throws Exception {
    String path =
        "/v6/items/identifier/"
            + Base64.encodeBase64String("foo:bar/baz".getBytes(StandardCharsets.UTF_8))
            + "möööp";

    when(itemService.getByIdentifier(any(String.class), any(String.class)))
        .thenReturn(Item.builder().build());

    testHttpGetWithExpectedStatus(path, HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }
}
