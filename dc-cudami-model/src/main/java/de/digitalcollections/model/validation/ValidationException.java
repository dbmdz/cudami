package de.digitalcollections.model.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends Exception {

  private List<ValidationError> errors = new ArrayList<>(1);

  public ValidationException(String msg, Exception e) {
    super(msg, e);
  }

  public ValidationException(String msg) {
    super(msg);
  }

  public void addError(ValidationError validationError) {
    errors.add(validationError);
  }

  public List<ValidationError> getErrors() {
    return errors;
  }
}
