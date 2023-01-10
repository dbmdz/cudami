package de.digitalcollections.cudami.admin.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class LanguageSortingHelper {

  private final MessageSource messageSource;
  private final List<Locale> prioritisedSortedLanguages;

  public LanguageSortingHelper(
      @Value("${cudami.prioritisedSortedLanguages}") List<Locale> prioritisedSortedLanguages,
      MessageSource messageSource) {
    this.messageSource = messageSource;
    this.prioritisedSortedLanguages = List.copyOf(prioritisedSortedLanguages);
  }

  protected String getLocalizedLanguageString(Locale locale, Locale displayLocale) {
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
