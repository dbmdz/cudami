package de.digitalcollections.model.semantic;

import de.digitalcollections.model.UniqueObject;
import java.util.Locale;
import lombok.experimental.SuperBuilder;

/**
 * See https://en.wikipedia.org/wiki/Headword and
 * https://de.wikipedia.org/wiki/Stichwort_(Dokumentation)
 *
 * <p>"A headword, head word, (lemma), or sometimes catchword, is the word under which a set of
 * related dictionary or encyclopaedia entries appears. The headword is used to locate the entry,
 * and dictates its alphabetical position."
 *
 * <p>(for difference to "lemma" see
 * http://www.differencebetween.net/language/difference-between-headword-and-lemma/)
 *
 * <p>As it is often interchangeable, we decided for practial reasons to use this class "Headword"
 * also for a lemma.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Headword extends UniqueObject {

  private String label;
  private String labelNormalized;
  private Locale locale;

  public Headword() {}

  public Headword(String label, Locale locale) {
    this.label = label;
    this.locale = locale;
  }

  public Headword(String label, String labelNormalized, Locale locale) {
    this(label, locale);
    this.labelNormalized = labelNormalized;
  }

  public String getLabel() {
    return label;
  }

  public String getLabelNormalized() {
    return labelNormalized;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * to allow project specific/language independent sorting/searching a normalized version of label
   * should be put here. e.g. project specific replacing of characters with diacritics with basic
   * form, like "É" normalized to "E" or "Č" to "C".
   */
  public void setLabelNormalized(String labelNormalized) {
    this.labelNormalized = labelNormalized;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
}
