package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {

  @Autowired
  LocaleService localeService;

  @GetMapping("/api/languages")
  public List<String> getLanguages() {
    return localeService.getSupportedLanguages();
  }
}
