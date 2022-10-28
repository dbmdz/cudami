package de.digitalcollections.cudami.admin.controller.advice;

import de.digitalcollections.cudami.admin.propertyeditor.LocalizedStructuredContentEditor;
import de.digitalcollections.cudami.admin.propertyeditor.LocalizedTextEditor;
import de.digitalcollections.cudami.admin.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.admin.propertyeditor.StructuredContentEditor;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalBindingInitializer {

  @Autowired private LocalizedStructuredContentEditor localizedStructuredContentEditor;

  @Autowired private LocalizedTextEditor localizedTextContentEditor;

  @Autowired private RoleEditor roleEditor;

  @Autowired private StructuredContentEditor structuredContentEditor;

  @InitBinder
  public void registerCustomEditors(WebDataBinder binder, WebRequest request) {
    binder.registerCustomEditor(LocalizedStructuredContent.class, localizedStructuredContentEditor);
    binder.registerCustomEditor(LocalizedText.class, localizedTextContentEditor);
    binder.registerCustomEditor(Role.class, roleEditor);
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    binder.registerCustomEditor(StructuredContent.class, structuredContentEditor);
  }
}
