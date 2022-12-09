package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
      value = {
        "/v6/languages/default",
        "/v5/languages/default",
        "/v2/languages/default",
        "/latest/languages/default"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Locale> getDefaultLanguage() throws ResourceNotFoundException {
    String defaultLanguage = localeService.getDefaultLanguage();
    if (defaultLanguage == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(new Locale(defaultLanguage), HttpStatus.OK);
  }

  @Operation(summary = "Get default locale")
  @GetMapping(
      value = {
        "/v6/locales/default",
        "/v5/locales/default",
        "/v2/locales/default",
        "/v1/locales/default",
        "/latest/locales/default"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Locale> getDefaultLocale() throws ResourceNotFoundException {
    Locale defaultLocale = localeService.getDefaultLocale();
    return new ResponseEntity<>(
        defaultLocale, defaultLocale != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all supported languages")
  @GetMapping(
      value = {"/v6/languages", "/v5/languages", "/v2/languages", "/latest/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> getSupportedLanguages() {
    return localeService.getSupportedLanguages();
  }

  @Operation(summary = "Get all supported locales")
  @GetMapping(
      value = {"/v6/locales", "/v5/locales", "/v2/locales", "/v1/locales", "/latest/locales"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getSupportedLocales() {
    return localeService.getSupportedLocales();
  }
}
