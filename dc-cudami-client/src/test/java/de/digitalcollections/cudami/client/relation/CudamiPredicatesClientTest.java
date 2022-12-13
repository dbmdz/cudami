package de.digitalcollections.cudami.client.relation;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.relation.Predicate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The PredicatesClient")
class CudamiPredicatesClientTest
    extends BaseCudamiRestClientTest<Predicate, CudamiPredicatesClient> {

  @Test
  @DisplayName("can update without uuid (old behaviour)")
  public void testUpdateWithoutUuid() throws Exception {
    Predicate predicate = Predicate.builder().value("foo").build();
    client.update(predicate);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody("put", "/foo", predicate);
  }

  @Test
  @DisplayName("can update with uuid")
  public void testUpdateWithUuid() throws Exception {
    UUID uuid = UUID.randomUUID();
    Predicate predicate = Predicate.builder().value("foo").uuid(uuid).build();
    client.update(uuid, predicate);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody("put", "/" + uuid, predicate);
  }

  @Test
  @DisplayName("can find all")
  // FIXME delete this method as soon as V7 breaking API is introduced
  @Override
  public void testFindAll() throws Exception {
    client.getAll();
    verifyHttpRequestByMethodAndRelativeURL("get", "");
  }

  @Test
  @DisplayName("can execute the find method with a basic PageRequest")
  // FIXME delete this method as soon as V7 breaking API is introduced
  @Override
  public void testFindWithPageRequest1() throws Exception {
    PageRequest pageRequest = new PageRequest();
    client.find(pageRequest);
    verifyHttpRequestByMethodAndRelativeURL("get", "/paged?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can find by PageRequest")
  // FIXME delete this method as soon as V7 breaking API is introduced
  @Override
  public void testFindWithPageRequest2() throws Exception {
    client.find(buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/paged?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&foo=eq:bar&gnarf=eq:krchch&searchTerm=hello");
  }
}
