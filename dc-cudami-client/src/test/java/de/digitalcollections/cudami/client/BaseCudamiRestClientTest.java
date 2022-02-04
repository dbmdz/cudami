package de.digitalcollections.cudami.client;

import de.digitalcollections.client.BaseRestClientTest;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;

public abstract class BaseCudamiRestClientTest<T extends Object, C extends CudamiRestClient<T>>
    extends BaseRestClientTest<T, C> {

  public BaseCudamiRestClientTest() {
    super(new DigitalCollectionsObjectMapper());
  }
}
