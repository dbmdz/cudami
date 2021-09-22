package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.identifiable.Identifiable;
import org.assertj.core.api.Assertions;

public class CudamiAssertions extends Assertions {

  public static IdentifiableAssert assertThat(Identifiable actual) {
    return new IdentifiableAssert(actual);
  }
}
