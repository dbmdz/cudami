package de.digitalcollections.cudami.client.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class BaseCudamiIdentifiablesClientTest<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends BaseCudamiRestClientTest<I, C> {

  /**
   * Creates an example PageRequest, which fills all possible fields:
   *
   * <ul>
   *   <li>first filter: Expression "foo" must be equal to "bar"
   *   <li>second filter: Expression "gnarf" must be equal to "krchch"
   *   <li>order: Descending for property "sortable" and nulls first
   *   <li>pageNumber: 1
   *   <li>pageSize: 2
   * </ul>
   *
   * @return example PageRequest with defined pageSize, pageNumber, sorting and two filters
   */
  protected PageRequest buildExamplePageRequest() {
    Direction direction = Direction.DESC;
    Order order = new Order(direction, true, NullHandling.NULLS_FIRST, "sortable");
    Sorting sorting = new Sorting(order);
    FilterCriterion filterCriterion1 = new FilterCriterion("foo", FilterOperation.EQUALS, "bar");
    FilterCriterion filterCriterion2 =
        new FilterCriterion("gnarf", FilterOperation.EQUALS, "krchch");
    Filtering filtering = new Filtering(List.of(filterCriterion1, filterCriterion2));
    return new PageRequest(1, 2, sorting, filtering);
  }

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
  @DisplayName("can access the inherited count endpoint")
  public void count() throws Exception {
    when(httpResponse.body()).thenReturn("42");

    assertThat(client.count()).isEqualTo(42);

    verifyHttpRequestByMethodAndRelativeURL("get", "/count");
  }

  @Test
  @DisplayName("can create an instance of the data type")
  public void createInstance() {
    I identifiable = client.create();
    assertThat(identifiable).isNotNull();
    assertThat(identifiable).isInstanceOf(objectType);
  }

  @Test
  @DisplayName("can find by language and initial with plain attributes")
  public void findByLanguageAndInitial() throws Exception {
    client.findByLanguageAndInitial(1, 2, "label", "ASC", "NATIVE", "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=label.asc");
  }

  @Test
  @DisplayName("can find by language, initial and full featured PageRequest")
  public void findByLanguageInitialPageRequest() throws Exception {
    PageRequest pageRequest = buildExamplePageRequest();
    client.findByLanguageAndInitial(pageRequest, "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

  @Test
  @DisplayName("can execute the find method with a PageRequest")
  public void findWithPageRequest() throws Exception {
    PageRequest pageRequest = new PageRequest();
    client.find(pageRequest);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  public void findWithSearchPageRequest() throws Exception {
    String bodyJson = "{}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<I> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "/search?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a search term and max results")
  public void findWithSearchTermAndMaxResults() throws Exception {
    String bodyJson = "{\"content\":[]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    assertThat(client.find("foo", 100)).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/search?pageNumber=0&pageSize=100&searchTerm=foo");
  }

  @Test
  @DisplayName("can get by identifier")
  public void getByIdentifier() throws Exception {
    String identifierNamespace = "gnd";
    String identifierValue = "1234567-8";

    client.getByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier/" + identifierNamespace + ":" + identifierValue + ".json");
  }

  @Test
  @DisplayName("can get by UUID only")
  public void getByUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getByUuid(uuid);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid);
  }

  @Test
  @DisplayName("can get by UUID and locale")
  public void getByUuidAndLocale() throws Exception {
    UUID uuid = UUID.randomUUID();
    Locale locale = Locale.GERMAN;
    client.getByUuidAndLocale(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("can get by UUID and locale as a string")
  public void getByUuidAndLocaleAsString() throws Exception {
    UUID uuid = UUID.randomUUID();
    String locale = Locale.GERMAN.toString();
    client.getByUuidAndLocale(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("can save an identifiable")
  public void save() throws Exception {
    I toSave = client.create();

    client.save(toSave);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody("post", "", toSave);
  }

  @Test
  @DisplayName("can update an identifiable by its uuid")
  public void update() throws Exception {
    UUID uuid = UUID.randomUUID();
    I toUpdate = client.create();
    toUpdate.setUuid(uuid);

    client.update(uuid, toUpdate);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody("put", "/" + uuid, toUpdate);
  }
}
