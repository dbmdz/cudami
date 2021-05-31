package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Locale and language controller")
public class LocaleController {

  private final LocaleService localeService;

  public LocaleController(LocaleService localeService) {
    this.localeService = localeService;
  }

  @Operation(summary = "Get default language")
  @GetMapping(
      value = {"/v5/languages/default", "/v2/languages/default", "/latest/languages/default"})
  public Locale getDefaultLanguage() {
    return new Locale(localeService.getDefaultLanguage());
  }

  @Operation(summary = "Get default locale")
  @GetMapping(
      value = {
        "/v5/locales/default",
        "/v2/locales/default",
        "/v1/locales/default",
        "/latest/locales/default"
      })
  public Locale getDefaultLocale() {
    return localeService.getDefaultLocale();
  }

  @Operation(summary = "Get all supported languages")
  @GetMapping(value = {"/v5/languages", "/v2/languages", "/latest/languages"})
  public List<String> getSupportedLanguages() {
    return localeService.getSupportedLanguages();
  }

  @Operation(summary = "Get all supported locales")
  @GetMapping(value = {"/v5/locales", "/v2/locales", "/v1/locales", "/latest/locales"})
  public List<Locale> getSupportedLocales() {
    return localeService.getSupportedLocales();
  }
}
