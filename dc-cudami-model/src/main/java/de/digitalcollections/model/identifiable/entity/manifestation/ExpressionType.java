package de.digitalcollections.model.identifiable.entity.manifestation;

import de.digitalcollections.model.MainSubType;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ExpressionType extends MainSubType {

  public ExpressionType() {
    super();
  }

  public ExpressionType(String mainType, String subType) {
    super(mainType, subType);
  }
}
