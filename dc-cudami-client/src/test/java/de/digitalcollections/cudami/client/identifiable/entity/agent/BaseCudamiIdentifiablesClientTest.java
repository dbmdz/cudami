package de.digitalcollections.cudami.client.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public abstract class BaseCudamiIdentifiablesClientTest<
    I extends Identifiable, C extends CudamiIdentifiablesClient<I>> {

  private final Class<C> clientType;
  private final Class<I> identifiableType;
  protected C client;
  protected String baseEndpoint;
  protected HttpClient httpClient;
  protected HttpResponse httpResponse;
  protected static final String SERVER_URL = "http://localhost:1234";
  private DigitalCollectionsObjectMapper mapper;

  protected ArgumentCaptor<HttpRequest> httpRequestCaptor;

  protected BaseCudamiIdentifiablesClientTest() {
    identifiableType =
        (Class<I>)
            ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    clientType =
        (Class<C>)
            ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
  }

  @BeforeEach
  public void beforeEach()
      throws IOException, InterruptedException, NoSuchMethodException, InvocationTargetException,
          InstantiationException, IllegalAccessException {
    httpClient = mock(HttpClient.class);
    httpResponse = mock(HttpResponse.class);
    httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    mapper = new DigitalCollectionsObjectMapper();

    // Instanciate the client with lots of magic because of the generics
    Class[] constructorArgTypes = new Class[3];
    constructorArgTypes[0] = HttpClient.class;
    constructorArgTypes[1] = String.class;
    constructorArgTypes[2] = ObjectMapper.class;

    client =
        clientType
            .getDeclaredConstructor(constructorArgTypes)
            .newInstance(httpClient, SERVER_URL, mapper);
    baseEndpoint = client.getBaseEndpoint();

    when(httpResponse.statusCode()).thenReturn(200);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(httpResponse);
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
    assertThat(identifiable).isInstanceOf(identifiableType);
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
  @DisplayName("can find by UUID only")
  public void findByUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.findOne(uuid);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid);
  }

  @Test
  @DisplayName("can find by UUID and locale")
  public void findByUuidAndLocale() throws Exception {
    UUID uuid = UUID.randomUUID();
    Locale locale = Locale.GERMAN;
    client.findOne(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("can find by UUID and locale as a string")
  public void findByUuidAndLocaleAsString() throws Exception {
    UUID uuid = UUID.randomUUID();
    String locale = Locale.GERMAN.toString();
    client.findOne(uuid, locale);

    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "?locale=" + locale);
  }

  @Test
  @DisplayName("can find by identifier")
  public void findByIdentifier() throws Exception {
    String identifierNamespace = "gnd";
    String identifierValue = "1234567-8";

    client.findOneByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier/" + identifierNamespace + ":" + identifierValue + ".json");
  }

  @Test
  @DisplayName("can find by language and initial with plain attributes")
  public void findByLanguageAndInitial() throws Exception {
    client.findByLanguageAndInitial(
        SERVER_URL + baseEndpoint, 1, 2, "label", "ASC", "NATIVE", "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=label.asc");
  }

  @Test
  @DisplayName("can find by language, initial and full featured PageRequest")
  public void findByLanguageInitialPageRequest() throws Exception {
    PageRequest pageRequest = buildExamplePageRequest();
    client.findByLanguageAndInitial(SERVER_URL + baseEndpoint, pageRequest, "de", "a");

    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

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

  protected void verifyHttpRequestByMethodAndRelativeURL(String method, String url)
      throws IOException, InterruptedException {
    verify(httpClient, times(1))
        .send(httpRequestCaptor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest actualRequest = httpRequestCaptor.getValue();
    assertThat(actualRequest.method()).isEqualToIgnoringCase(method);
    assertThat(actualRequest.uri()).isEqualTo(URI.create(SERVER_URL + baseEndpoint + url));
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

  protected void verifyHttpRequestByMethodRelativeUrlAndRequestBody(
      String method, String url, Object requestBodyObject)
      throws IOException, InterruptedException {
    verify(httpClient, times(1))
        .send(httpRequestCaptor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest actualRequest = httpRequestCaptor.getValue();
    assertThat(actualRequest.method()).isEqualToIgnoringCase(method);
    assertThat(actualRequest.uri()).isEqualTo(URI.create(SERVER_URL + baseEndpoint + url));

    // This is according to https://stackoverflow.com/a/59347350 the way to verify, if the
    // body of the HTTP request carries the expected serialized Identifiable object
    HttpRequest.BodyPublisher expectedBodyPublisher =
        HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBodyObject));
    FlowSubscriber<ByteBuffer> expectedFlowSubscriber = new FlowSubscriber<>();
    expectedBodyPublisher.subscribe(expectedFlowSubscriber);
    byte[] expected = expectedFlowSubscriber.getBodyItems().get(0).array();

    FlowSubscriber<ByteBuffer> actualFlowSubscriber = new FlowSubscriber<>();
    HttpRequest.BodyPublisher actualBodyPublisher = actualRequest.bodyPublisher().get();
    actualBodyPublisher.subscribe(actualFlowSubscriber);
    byte[] actual = actualFlowSubscriber.getBodyItems().get(0).array();

    assertThat(new String(actual)).isEqualTo(new String(expected));
  }

  // --------------------------------------------------------------------
  public static class FlowSubscriber<T> implements Flow.Subscriber<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private List<T> bodyItems = new ArrayList<>();

    public List<T> getBodyItems() {
      try {
        this.latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return bodyItems;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      // Retrieve all parts
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T item) {
      this.bodyItems.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
      this.latch.countDown();
    }

    @Override
    public void onComplete() {
      this.latch.countDown();
    }
  }
}
