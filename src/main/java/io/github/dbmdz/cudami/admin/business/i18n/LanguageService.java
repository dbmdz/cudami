package io.github.dbmdz.cudami.admin.business.i18n;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LanguageService {

  private final CudamiLocalesClient localeService;
  private final MessageSource messageSource;
  private final List<Locale> prioritisedSortedLanguages;

  public LanguageService(
      CudamiClient client,
      @Value("${cudami.prioritisedSortedLanguages}") List<Locale> prioritisedSortedLanguages,
      MessageSource messageSource) {
    this.localeService = client.forLocales();
    this.messageSource = messageSource;
    this.prioritisedSortedLanguages = List.copyOf(prioritisedSortedLanguages);
  }

  public List<Locale> getAllLanguages() throws TechnicalException {
    List<Locale> allLanguagesAsLocales = localeService.getAllLanguagesAsLocales();
    final Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> sortedLanguages = sortLanguages(displayLocale, allLanguagesAsLocales);
    return sortedLanguages;
  }

  public List<Locale> getAllLanguagesAsLocales() throws TechnicalException {
    List<Locale> allLocales = getAllLocales();
    return allLocales.stream().filter(l -> l.getCountry().isBlank()).collect(Collectors.toList());
  }

  public List<String> getAllLanguagesAsString() throws TechnicalException {
    return localeService.getAllLanguages();
  }

  public List<Locale> getAllLocales() throws TechnicalException {
    return localeService.getAllLocales();
  }

  public Locale getDefaultLanguage() throws TechnicalException {
    return localeService.getDefaultLanguage();
  }

  public String getDefaultLocale() throws TechnicalException {
    return localeService.getDefaultLocale();
  }

  public List<Locale> getExistingLanguages(Locale defaultLanguage, LocalizedText localizedText) {
    List<Locale> existingLanguages = List.of(defaultLanguage);
    if (!CollectionUtils.isEmpty(localizedText)) {
      return getExistingLanguagesForLocales(localizedText.getLocales());
    }
    return existingLanguages;
  }

  public List<Locale> getExistingLanguages(LocalizedText localizedText) {
    if (!CollectionUtils.isEmpty(localizedText)) {
      return getExistingLanguagesForLocales(localizedText.getLocales());
    }
    return null;
  }

  public List<Locale> getExistingLanguagesForLocales(List<Locale> locales) {
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(locales)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages = sortLanguages(displayLocale, locales);
    }
    return existingLanguages;
  }

  public String getLocalizedLanguageString(Locale locale, Locale displayLocale) {
    String localizedLanguageString = locale.getDisplayLanguage(displayLocale);
    if (localizedLanguageString.isBlank()) {
      String language = locale.getLanguage();
      if ("und".equalsIgnoreCase(language)) {
        localizedLanguageString =
            messageSource.getMessage("language_not_specified", null, displayLocale);
      }
    }
    return localizedLanguageString;
  }

  public List<Locale> sortLanguages(Locale displayLocale, Collection<Locale> languagesToSort) {
    List<Locale> sortedLanguages =
        prioritisedSortedLanguages.stream()
            .filter(languagesToSort::contains)
            .collect(Collectors.toList());
    languagesToSort.stream()
        .filter(l -> !prioritisedSortedLanguages.contains(l))
        .sorted(Comparator.comparing(l -> getLocalizedLanguageString(l, displayLocale)))
        .distinct()
        .forEach(sortedLanguages::add);
    return sortedLanguages;
  }
}
