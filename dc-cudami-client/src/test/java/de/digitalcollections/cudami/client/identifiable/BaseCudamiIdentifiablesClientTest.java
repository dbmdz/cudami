package de.digitalcollections.cudami.client.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class BaseCudamiIdentifiablesClientTest<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends BaseCudamiRestClientTest<I, C> {

  @Test
  @DisplayName("can find by language and initial with plain attributes")
  public void testFindByLanguageAndInitial() throws Exception {
    client.findByLanguageAndInitial(1, 2, "label", "ASC", "NATIVE", "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=label.asc.ignorecase");
  }

  @Test
  @DisplayName("can find by language, initial string and dedicated paging attributes")
  public void testFindByLanguageInitialAndPagingAttributes() throws Exception {
    client.findByLanguageAndInitial(1, 2, "sortable", "asc", "NATIVE", "de", "a");
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.asc.ignorecase");
  }

  @Test
  @DisplayName("can find by language, initial and full featured PageRequest")
  public void testFindByLanguageInitialPageRequest() throws Exception {
    PageRequest pageRequest = buildExamplePageRequest();
    client.findByLanguageAndInitial(pageRequest, "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&filtering=foo:eq:bar;gnarf:eq:krchch");
  }

  @Test
  @DisplayName("can execute the find method with a PageRequest")
  public void testFindWithPageRequest() throws Exception {
    String bodyJson = "{" + "\"listResponseType\":\"PAGE_RESPONSE\"" + "}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    PageRequest pageRequest = new PageRequest();
    PageResponse<I> response = client.find(pageRequest);
    assertThat(response).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can get by UUID and locale")
  public void testGetByUuidAndLocale() throws Exception {
    UUID uuid = UUID.randomUUID();
    Locale locale = Locale.GERMAN;
    client.getByUuidAndLocale(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("can get by UUID and locale as a string")
  public void testGetByUuidAndLocaleAsString() throws Exception {
    UUID uuid = UUID.randomUUID();
    String locale = Locale.GERMAN.toString();
    client.getByUuidAndLocale(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("calls getByIdentifier with Base64 URL safe encoded data")
  public void testGetByIdentifierBase64Encoded() throws Exception {
    client.getByIdentifier("foo", "bar/baz");
    verifyHttpRequestByMethodAndRelativeURL("get", "/identifier/Zm9vOmJhci9iYXo");
  }
}
