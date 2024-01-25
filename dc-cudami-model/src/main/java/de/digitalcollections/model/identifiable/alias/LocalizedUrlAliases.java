package de.digitalcollections.model.identifiable.alias;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LocalizedUrlAliases extends HashMap<Locale, List<UrlAlias>> {

  /** Default constructor, is needed by Jackson */
  public LocalizedUrlAliases() {
    super();
  }

  public LocalizedUrlAliases(UrlAlias... urlAliases) {
    super();
    this.add(urlAliases);
  }

  public LocalizedUrlAliases(List<UrlAlias> urlAliases) {
    this(urlAliases.toArray(UrlAlias[]::new));
  }

  public void add(UrlAlias... urlAliases) {
    if (urlAliases == null) {
      return;
    }
    for (UrlAlias urlAlias : urlAliases) {
      if (urlAlias == null) {
        continue;
      }
      if (urlAlias.getTargetLanguage() == null) {
        throw new IllegalArgumentException(
            "Missing mandatory targetLanguage for urlAlias=" + urlAlias);
      }
      this.compute(
          urlAlias.getTargetLanguage(),
          (locale, listOfAliases) -> {
            if (listOfAliases == null) {
              listOfAliases = new ArrayList<>();
            }
            listOfAliases.add(urlAlias);
            return listOfAliases;
          });
    }
  }

  /**
   * @param urlAlias url alias to be checked
   * @return true if the passed {@code UrlAlias} is contained in any of the locale specific lists
   */
  public boolean containsUrlAlias(UrlAlias urlAlias) {
    return this.flatten().contains(urlAlias);
  }

  @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION", justification = "to be reviewed later")
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LocalizedUrlAliases)) {
      return false;
    }
    LocalizedUrlAliases other = (LocalizedUrlAliases) o;
    try {
      if (this.flatten().size() != other.flatten().size()) {
        return false;
      }
      return new HashSet<>(flatten()).containsAll(other.flatten());
    } catch (NullPointerException | ClassCastException unused) {
      return false;
    }
  }

  /**
   * Flatten this map to a list.
   *
   * @return list containing all {@code UrlAlias}es from this object
   */
  public List<UrlAlias> flatten() {
    return this.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  public List<Locale> getTargetLanguages() {
    return new ArrayList<>(this.keySet());
  }

  public boolean hasTargetLanguage(Locale locale) {
    return this.containsKey(locale);
  }
}
