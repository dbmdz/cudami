package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.identifiable.Identifiable;
import org.assertj.core.api.AbstractAssert;

/**
 * The IdentifiableAssert compares to identifiables field by field with the exception of
 * lastModified.
 */
public class IdentifiableAssert extends AbstractAssert<IdentifiableAssert, Identifiable> {

  Identifiable actual;

  public IdentifiableAssert(Identifiable identifiable) {
    super(identifiable, IdentifiableAssert.class);
    actual = identifiable;
  }

  public static IdentifiableAssert assertThat(Identifiable identifiable) {
    return new IdentifiableAssert(identifiable);
  }

  @Override
  public IdentifiableAssert isEqualTo(Object expected) {
    if (expected instanceof Identifiable) {
      ((Identifiable) expected).setLastModified(actual.getLastModified());
    }

    return super.isEqualTo(expected);
  }
}
