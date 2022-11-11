package de.digitalcollections.cudami.admin.business.impl.validator;

import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.i18n.LocaleContextHolder;
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
    if (localizedText == null || localizedText.isEmpty()) {
      errors.reject("label", "validation.label.NotBlank");
      return;
    }
    Locale displayLocale = LocaleContextHolder.getLocale();
    for (Map.Entry<Locale, String> entry : localizedText.entrySet()) {
      Locale locale = entry.getKey();
      String text = entry.getValue();
      if (text == null || text.isBlank()) {
        errors.reject(
            "validation.label.language.NotBlank",
            new String[] {locale.getDisplayLanguage(displayLocale)},
            "validation.label.NotBlank");
        // error message key: see hibernate-validator library:
        errors.rejectValue(
            "label['" + locale.getLanguage() + "']",
            "javax.validation.constraints.NotBlank.message");
      }
    }
  }
}
