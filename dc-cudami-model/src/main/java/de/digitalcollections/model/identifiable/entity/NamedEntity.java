package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.text.LocalizedText;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public interface NamedEntity {

  LocalizedText getName();

  void setName(LocalizedText name);

  Set<Locale> getNameLocalesOfOriginalScripts();

  void setNameLocalesOfOriginalScripts(Set<Locale> localesOfOriginalScripts);

  default void addNameLocaleOfOriginalScript(Locale locale) {
    if (getNameLocalesOfOriginalScripts() == null) {
      setNameLocalesOfOriginalScripts(new HashSet<>(Set.of(locale)));
    } else {
      getNameLocalesOfOriginalScripts().add(locale);
    }
  }
}
