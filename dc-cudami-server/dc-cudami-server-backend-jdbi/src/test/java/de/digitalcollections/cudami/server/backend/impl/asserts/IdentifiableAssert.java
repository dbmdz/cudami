package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.identifiable.Identifiable;
import org.assertj.core.api.AbstractAssert;

/**
 * The IdentifiableAssert compares two identifiables field by field with the exception of
 * lastModified.
 */
public class IdentifiableAssert extends AbstractAssert<IdentifiableAssert, Identifiable> {

  private Identifiable actual;

  public IdentifiableAssert(Identifiable identifiable) {
    super(identifiable, IdentifiableAssert.class);
    actual = identifiable;
  }

  @Override
  public IdentifiableAssert isEqualTo(Object expected) {
    if (expected instanceof Identifiable) {
      ((Identifiable) expected).setLastModified(actual.getLastModified());
    }

    return super.isEqualTo(expected);
  }
}
