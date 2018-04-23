package de.digitalcollections.cudami.admin.controller.advice;

import de.digitalcollections.cudami.admin.propertyeditor.MultilanguageDocumentEditor;
import de.digitalcollections.cudami.admin.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.model.api.identifiable.resource.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalBindingInitializer {

  @Autowired
  MultilanguageDocumentEditor multilanguageDocumentEditor;

  @Autowired
  RoleEditor roleEditor;

  @InitBinder
  public void registerCustomEditors(WebDataBinder binder, WebRequest request) {
    binder.registerCustomEditor(MultilanguageDocument.class, multilanguageDocumentEditor);
    binder.registerCustomEditor(Role.class, roleEditor);
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }
}
