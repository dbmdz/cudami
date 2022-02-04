package de.digitalcollections.cudami.client.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class BaseCudamiIdentifiablesClientTest<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends BaseCudamiRestClientTest<I, C> {

  /**
   * Creates an example SearchPageRequest, which fills all possible fields:
   *
   * <ul>
   *   <li>order: Descending for property "sortable" and nulls first
   *   <li>pageNumber: 1
   *   <li>pageSize: 2
   *   <li>searchTerm: "foo"
   * </ul>
   *
   * @return example SearchPageRequest with defined pageSize, pageNumber, sorting and searchTerm
   */
  protected SearchPageRequest buildExampleSearchPageRequest() {
    Direction direction = Direction.DESC;
    Order order = new Order(direction, true, NullHandling.NULLS_FIRST, "sortable");
    Sorting sorting = new Sorting(order);
    return new SearchPageRequest("foo", 1, 2, sorting);
  }

  @Test
  @DisplayName("can find by language and initial with plain attributes")
  public void testFindByLanguageAndInitial() throws Exception {
    client.findByLanguageAndInitial(1, 2, "label", "ASC", "NATIVE", "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=label.asc");
  }

  @Test
  @DisplayName("can find by language, initial string and dedicated paging attributes")
  public void testFindByLanguageInitialAndPagingAttributes() throws Exception {
    client.findByLanguageAndInitial(1, 2, "sortable", "asc", "NATIVE", "de", "a");
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.asc");
  }

  @Test
  @DisplayName("can find by language, initial and full featured PageRequest")
  public void testFindByLanguageInitialPageRequest() throws Exception {
    PageRequest pageRequest = buildExamplePageRequest();
    client.findByLanguageAndInitial(pageRequest, "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  public void testFindWithSearchPageRequest() throws Exception {
    String bodyJson = "{}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<I> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "/search?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a search term and max results")
  public void testFindWithSearchTermAndMaxResults() throws Exception {
    String bodyJson = "{\"content\":[]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    assertThat(client.find("foo", 100)).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/search?pageNumber=0&pageSize=100&searchTerm=foo");
  }

  @Test
  @DisplayName("can get by identifier")
  public void testGetByIdentifier() throws Exception {
    String identifierNamespace = "gnd";
    String identifierValue = "1234567-8";

    client.getByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier/" + identifierNamespace + ":" + identifierValue + ".json");
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
}
