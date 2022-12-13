package de.digitalcollections.cudami.admin.controller.fragments;

import de.digitalcollections.model.identifiable.Identifiable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FragmentsController {

  @GetMapping(value = "/fragments/forms/label-description")
  public String getFormFragmentLabelDescription(
      @RequestParam(name = "fieldLanguage", required = true) String fieldLanguage, Model model) {
    model.addAttribute("identifiable", new Identifiable());
    model.addAttribute("beanName", "identifiable");
    model.addAttribute("fieldLanguage", fieldLanguage);
    model.addAttribute("active", true);
    return "fragments/forms/label-description-form";
  }
}
