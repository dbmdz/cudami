package de.digitalcollections.cudami.admin.business.impl.validator;

import de.digitalcollections.model.text.LocalizedText;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class LabelNotBlankValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return LocalizedText.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    LocalizedText localizedText = (LocalizedText) target;
    if (localizedText == null
        || localizedText.isEmpty()
        || localizedText.values().stream().allMatch(t -> t == null || t.trim().isBlank())) {
      // error message key: see hibernate-validator library
      errors.rejectValue("label", "javax.validation.constraints.NotBlank.message");
    }
  }
}
