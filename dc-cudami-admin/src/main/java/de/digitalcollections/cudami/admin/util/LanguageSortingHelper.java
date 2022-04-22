package de.digitalcollections.cudami.admin.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageSortingHelper {
  List<Locale> prioritisedSortedLanguages;

  public LanguageSortingHelper(List<Locale> prioritisedSortedLanguages) {
    this.prioritisedSortedLanguages = List.copyOf(prioritisedSortedLanguages);
  }

  public List<Locale> sortLanguages(Locale displayLocale, Collection<Locale> languagesToSort) {
    List<Locale> sortedLanguages =
        prioritisedSortedLanguages.stream()
            .filter(languagesToSort::contains)
            .collect(Collectors.toList());
    languagesToSort.stream()
        .filter(l -> !prioritisedSortedLanguages.contains(l))
        .sorted(Comparator.comparing(l -> l.getDisplayLanguage(displayLocale)))
        .distinct()
        .forEach(sortedLanguages::add);
    return sortedLanguages;
  }
}
