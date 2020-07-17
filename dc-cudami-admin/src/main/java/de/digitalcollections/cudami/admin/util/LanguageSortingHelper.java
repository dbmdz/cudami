package de.digitalcollections.cudami.admin.util;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageSortingHelper {
  List<Locale> prioritisedSortedLanguages;

  public LanguageSortingHelper(List<Locale> prioritisedSortedLanguages) {
    this.prioritisedSortedLanguages = prioritisedSortedLanguages;
  }

  public List<Locale> sortLanguages(Locale displayLocale, List<Locale> languagesToSort) {
    List<Locale> sortedLanguages =
        prioritisedSortedLanguages.stream()
            .filter(languagesToSort::contains)
            .collect(Collectors.toList());
    languagesToSort.stream()
        .filter(l -> !prioritisedSortedLanguages.contains(l))
        .sorted(Comparator.comparing(l -> l.getDisplayLanguage(displayLocale)))
        .forEach(sortedLanguages::add);
    return sortedLanguages;
  }
}
