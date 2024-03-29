package io.github.dbmdz.cudami.controller;

import de.digitalcollections.model.exception.TechnicalException;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageAPIController {

  private final LanguageService service;

  public LanguageAPIController(LanguageService languageService) {
    this.service = languageService;
  }

  @GetMapping("/api/languages")
  public List<String> getAllLanguages() throws TechnicalException {
    return service.getAllLanguagesAsString();
  }

  @GetMapping("/api/languages/default")
  public Locale getDefaultLanguages() throws TechnicalException {
    return service.getDefaultLanguage();
  }
}
