package io.github.dbmdz.cudami.admin.controller.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import io.github.dbmdz.cudami.admin.propertyeditor.JsonObjectEditor;
import io.github.dbmdz.cudami.admin.propertyeditor.RoleEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalBindingInitializer {

  @Autowired private ObjectMapper objectMapper;
  @Autowired private RoleEditor roleEditor;

  @InitBinder
  public void registerCustomEditors(WebDataBinder binder, WebRequest request) {
    binder.registerCustomEditor(
        ImageFileResource.class, new JsonObjectEditor(objectMapper, ImageFileResource.class));
    binder.registerCustomEditor(
        LocalizedStructuredContent.class,
        new JsonObjectEditor(objectMapper, LocalizedStructuredContent.class));
    binder.registerCustomEditor(
        LocalizedText.class, new JsonObjectEditor(objectMapper, LocalizedText.class));
    binder.registerCustomEditor(
        RenderingHintsPreviewImage.class,
        new JsonObjectEditor(objectMapper, RenderingHintsPreviewImage.class));
    binder.registerCustomEditor(Role.class, roleEditor);
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    binder.registerCustomEditor(
        StructuredContent.class, new JsonObjectEditor(objectMapper, StructuredContent.class));
  }
}
