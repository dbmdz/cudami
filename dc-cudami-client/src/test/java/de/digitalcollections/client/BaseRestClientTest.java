package de.digitalcollections.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.Filtering;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public abstract class BaseRestClientTest<T extends Object, C extends BaseRestClient<T>> {
  protected static final String SERVER_URL = "http://localhost:1234";
  protected String baseEndpoint;
  protected C client;
  private Class<C> clientType;
  protected HttpClient httpClient;
  protected ArgumentCaptor<HttpRequest> httpRequestCaptor;
  protected HttpResponse httpResponse;
  protected final ObjectMapper mapper;
  protected Class<T> objectType;

  public BaseRestClientTest(ObjectMapper mapper) {
    this.mapper = mapper;
    objectType =
        (Class<T>)
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
    // Instanciate the client with lots of magic because of the generics
    Class[] constructorArgTypes = new Class[3];
    constructorArgTypes[0] = HttpClient.class;
    constructorArgTypes[1] = String.class;
    constructorArgTypes[2] = ObjectMapper.class;
    client =
        clientType
            .getDeclaredConstructor(constructorArgTypes)
            .newInstance(httpClient, BaseRestClientTest.SERVER_URL, mapper);
    baseEndpoint = client.getBaseEndpoint();
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(httpResponse);
  }

  @Test
  @DisplayName("params for filtering")
  protected void testFilteringParams() {
    LocalDate date = LocalDate.now();
    FilterCriterion fcDate =
        FilterCriterion.builder().withExpression("lastModified").isEquals(date).build();
    String expDate = String.format("lastModified:eq:%s", date.toString());
    assertThat(client.filterCriterionToUrlParam(fcDate)).isEqualTo(expDate);
  }

  @Test
  @DisplayName("complex Filtering with OR")
  protected void testComplexFilterParams() {
    Filtering filtering =
        Filtering.builder()
            .filterCriterion(
                FilterLogicalOperator.OR,
                FilterCriterion.builder()
                    .withExpression("label.und-Latn")
                    .contains("some search text")
                    .build())
            .filterCriterion(
                FilterLogicalOperator.OR,
                FilterCriterion.builder()
                    .withExpression("description.und-Latn")
                    .contains("some search text")
                    .build())
            .filterCriterion(
                FilterLogicalOperator.AND,
                FilterCriterion.builder().withExpression("active").isEquals(true).build())
            .build();
    String expected =
        "filtering=%7B$OR;label.und-Latn:like:some+search+text;description.und-Latn:like:some+search+text%7D;%7B$AND;active:eq:true%7D";
    assertThat(client.getFilterParamsAsString(filtering)).isEqualTo(expected);
  }

  @Test
  @DisplayName("simple Filtering syntax")
  protected void testSimpleFilterParams() {
    LocalDate date1 = LocalDate.now();
    LocalDate date2 = LocalDate.now().plusWeeks(1);
    Filtering filtering =
        Filtering.builder()
            .filterCriterion(
                FilterLogicalOperator.AND,
                FilterCriterion.builder()
                    .withExpression("label.und-Latn")
                    .contains("some search text")
                    .build())
            .filterCriterion(
                FilterLogicalOperator.AND,
                FilterCriterion.builder()
                    .withExpression("lastModified")
                    .between(date1, date2)
                    .build())
            .build();
    String expected =
        "filtering=label.und-Latn:like:some+search+text;lastModified:btn:"
            + date1.toString()
            + ","
            + date2.toString();
    assertThat(client.getFilterParamsAsString(filtering)).isEqualTo(expected);
  }

  protected void verifyHttpRequestByMethodAndRelativeURL(String method, String url)
      throws IOException, InterruptedException {
    verify(httpClient, times(1))
        .send(httpRequestCaptor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest actualRequest = httpRequestCaptor.getValue();
    assertThat(actualRequest.method()).isEqualToIgnoringCase(method);
    assertThat(actualRequest.uri()).isEqualTo(URI.create(SERVER_URL + baseEndpoint + url));
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
  private static class FlowSubscriber<T> implements Flow.Subscriber<T> {

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
