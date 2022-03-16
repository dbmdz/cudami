package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import org.assertj.core.api.Assertions;

public class CudamiAssertions extends Assertions {

  public static UniqueObjectAssert assertThat(UniqueObject actual) {
    return new UniqueObjectAssert(actual);
  }

  public static EntityAssert assertThat(Entity actual) {
    return new EntityAssert(actual);
  }
}
