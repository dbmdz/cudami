package de.digitalcollections.model.validation;

import java.io.Serializable;

public class ValidationError implements Serializable {

  private static final long serialVersionUID = 1L;
  private final String fieldName;
  private final String messageKey;

  public ValidationError(String fieldName, String messageKey) {
    this.fieldName = fieldName;
    this.messageKey = messageKey;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
