package de.digitalcollections.cudami.client.identifiable.entity;

import de.digitalcollections.cudami.client.identifiable.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.identifiable.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class BaseCudamiEntitiesClientTest<
        E extends Entity, C extends CudamiEntitiesClient<E>>
    extends BaseCudamiIdentifiablesClientTest<E, C> {

  @Test
  @DisplayName("can get an Entity by its refId")
  public void testGetByRefId() throws Exception {
    client.getByRefId(42);
    verifyHttpRequestByMethodAndRelativeURL("get", "/42");
  }
}
