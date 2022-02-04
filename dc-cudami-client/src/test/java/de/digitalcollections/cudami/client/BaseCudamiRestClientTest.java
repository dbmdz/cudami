package de.digitalcollections.cudami.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.client.BaseRestClientTest;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.UUID;
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
  public void testCreate() {
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
  @DisplayName("can find all")
  public void testFindAll() throws Exception {
    client.findAll();
    verifyHttpRequestByMethodAndRelativeURL("get", "/all");
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
        "?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
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
}
