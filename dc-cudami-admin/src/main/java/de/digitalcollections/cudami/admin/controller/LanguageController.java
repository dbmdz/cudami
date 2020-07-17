package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanguageController {

  private final CudamiLocalesClient service;

  public LanguageController(CudamiClient cudamiClient) {
    this.service = cudamiClient.forLocales();
  }

  @GetMapping("/api/languages")
  public List<String> getLanguages() throws Exception {
    return service.findAllLanguages();
  }

  @GetMapping("/api/languages/default")
  public Locale getDefaultLanguages() throws Exception {
    return service.getDefaultLanguage();
  }
}
