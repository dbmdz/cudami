package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.UniqueObject;
import org.assertj.core.api.AbstractAssert;

/**
 * The IdentifiableAssert compares two identifiables field by field with the exception of
 * lastModified.
 */
public class UniqueObjectAssert extends AbstractAssert<UniqueObjectAssert, UniqueObject> {

  private UniqueObject actual;

  public UniqueObjectAssert(UniqueObject uniqueObject) {
    super(uniqueObject, UniqueObjectAssert.class);
    actual = uniqueObject;
  }

  @Override
  public UniqueObjectAssert isEqualTo(Object expected) {
    if (expected instanceof UniqueObject) {
      ((UniqueObject) expected).setLastModified(actual.getLastModified());
      ((UniqueObject) expected).setCreated(actual.getCreated());
    }

    return super.isEqualTo(expected);
  }
}
