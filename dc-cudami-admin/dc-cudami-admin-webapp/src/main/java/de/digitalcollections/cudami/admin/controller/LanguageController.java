package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {

  LocaleRepository repository;

  @Autowired
  public LanguageController(LocaleRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/api/languages")
  public List<String> getLanguages() {
    return repository.findAllLanguages();
  }

  @GetMapping("/api/languages/default")
  public Locale getDefaultLanguages() {
    return repository.getDefaultLanguage();
  }
}
