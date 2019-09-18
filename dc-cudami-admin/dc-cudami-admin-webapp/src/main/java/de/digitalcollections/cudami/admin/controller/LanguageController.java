package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.admin.backend.impl.repository.LocaleRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {

  @Autowired
  LocaleRepository repository;

  @GetMapping("/api/languages")
  public List<String> getLanguages() {
    return repository.findAllLanguages();
  }
}
