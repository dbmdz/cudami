package de.digitalcollections.cudami.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.client.BaseRestClientTest;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class BaseCudamiRestClientTest<
        T extends UniqueObject, C extends CudamiRestClient<T>>
    extends BaseRestClientTest<T, C> {

  public BaseCudamiRestClientTest() {
    super(new DigitalCollectionsObjectMapper());
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

  @Test
  @DisplayName("can access the inherited count endpoint")
  public void testCount() throws Exception {
    when(httpResponse.body()).thenReturn("42");
    assertThat(client.count()).isEqualTo(42);
    verifyHttpRequestByMethodAndRelativeURL("get", "/count");
  }

  @Test
  @DisplayName("can create an instance of the data type")
  public void testCreate() throws TechnicalException {
    T object = client.create();
    assertThat(object).isNotNull();
    assertThat(object).isInstanceOf(objectType);
  }

  @Test
  @DisplayName("can delete by UUID")
  public void testDeleteByUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.deleteByUuid(uuid);
    verifyHttpRequestByMethodAndRelativeURL("delete", "/" + uuid);
  }

  @Test
  @DisplayName("can execute the find method with a basic PageRequest")
  public void testFindWithPageRequest1() throws Exception {
    PageRequest pageRequest = new PageRequest();
    client.find(pageRequest);
    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can find by PageRequest")
  public void testFindWithPageRequest2() throws Exception {
    client.find(buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&filtering=foo:eq:bar;gnarf:eq:krchch");
  }

  @Test
  @DisplayName("can get by UUID only")
  public void testGetByUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getByUuid(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid);
  }

  @Test
  @DisplayName("can save an object")
  public void testSave() throws Exception {
    T toSave = client.create();
    client.save(toSave);
    verifyHttpRequestByMethodRelativeUrlAndRequestBody("post", "", toSave);
  }

  @Test
  @DisplayName("can update an object by its uuid")
  public void testUpdate() throws Exception {
    UUID uuid = UUID.randomUUID();
    T toUpdate = client.create();
    toUpdate.setUuid(uuid);

    client.update(uuid, toUpdate);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody("put", "/" + uuid, toUpdate);
  }

  @DisplayName("can retrieve less than 30 uuids by GET")
  @Test
  public void getByUuidsGET() throws TechnicalException, IOException, InterruptedException {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    client.getByUuids(List.of(uuid1, uuid2));

    verifyHttpRequestByMethodAndRelativeURL("get", "/list/" + uuid1 + "," + uuid2);
  }

  @DisplayName("can retrieve more than 30 uuids by POST")
  @Test
  public void getByUuidsPOST() throws TechnicalException, IOException, InterruptedException {
    List<UUID> uuids =
        IntStream.of(0, 50).mapToObj(i -> UUID.randomUUID()).collect(Collectors.toList());

    client.getByUuids(uuids);

    String requestBody = new DigitalCollectionsObjectMapper().writeValueAsString(uuids);
    verifyHttpRequestByMethodRelativeUrlAndRequestBody("post", "/list", requestBody);
  }
}
