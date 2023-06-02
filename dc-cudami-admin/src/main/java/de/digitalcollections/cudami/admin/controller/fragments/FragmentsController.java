package de.digitalcollections.cudami.admin.controller.fragments;

import de.digitalcollections.model.identifiable.Identifiable;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FragmentsController {

  @GetMapping(value = "/fragments/forms/label", produces = MediaType.TEXT_HTML_VALUE)
  public String getFormFragmentLabel(
      @RequestParam(name = "fieldLanguage", required = true) String fieldLanguage, Model model) {
    model.addAttribute("identifiable", new Identifiable());
    model.addAttribute("beanName", "identifiable");
    model.addAttribute("fieldLanguage", fieldLanguage);
    model.addAttribute("active", true);
    return "fragments/forms/multilanguage-label-form";
  }

  @GetMapping(value = "/fragments/forms/label-description")
  public String getFormFragmentLabelDescription(
      @RequestParam(name = "fieldLanguage", required = true) String fieldLanguage, Model model) {
    model.addAttribute("identifiable", new Identifiable());
    model.addAttribute("beanName", "identifiable");
    model.addAttribute("fieldLanguage", fieldLanguage);
    model.addAttribute("active", true);
    return "fragments/forms/multilanguage-label-description-form";
  }

  @GetMapping(value = "/fragments/forms/label-loctextdescription")
  public String getFormFragmentLabelLocalizedTextDescription(
      @RequestParam(name = "fieldLanguage", required = true) String fieldLanguage, Model model) {
    model.addAttribute("identifiable", new Identifiable());
    model.addAttribute("beanName", "identifiable");
    model.addAttribute("fieldLanguage", fieldLanguage);
    model.addAttribute("active", true);
    return "fragments/forms/multilanguage-label-loctextdescription-form";
  }

  @GetMapping(value = "/fragments/forms/teaser")
  public String getFormFragmentTeaser(
      @RequestParam(name = "fieldLanguage", required = true) String fieldLanguage, Model model) {
    model.addAttribute("identifiable", new Identifiable());
    model.addAttribute("beanName", "identifiable");
    model.addAttribute("fieldLanguage", fieldLanguage);
    model.addAttribute("active", true);
    return "fragments/forms/multilanguage-teaser";
  }

  public static final String getDisplayLanguages(Locale displayLocale, Collection<Locale> locales) {
    if (locales == null || locales.isEmpty()) {
      return "";
    }
    return locales.stream()
        .map(
            l ->
                l.getDisplayLanguage(displayLocale)
                    + ((l.getDisplayScript(displayLocale) != null
                            && l.getDisplayScript().length() > 0)
                        ? " (" + l.getDisplayScript(displayLocale) + ")"
                        : ""))
        .collect(Collectors.joining(", "));
  }

  public static final String getDisplayLanguage(Locale displayLocale, Locale locale) {
    return locale == null ? null : getDisplayLanguages(displayLocale, Set.of(locale));
  }
}
