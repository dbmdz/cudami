package de.digitalcollections.model.util;

import com.ibm.icu.text.Transliterator;
import java.util.Locale;

/**
 * Helper class to transform "special" characters into a slug compatible and human-readable form.
 */
public class SlugGenerator {

  private final Transliterator transliterator;
  private int maxLength = -1;

  /** Default constructor for transliteration of german umlauts and further latin characters */
  public SlugGenerator() {
    this("Any-Latin; de-ASCII");
  }

  /**
   * Constructor, if other transliteration than german umlauts and further latin characters is
   * required.
   *
   * @param transliteratorId the ids for the transliteration charset(s)
   * @see <a
   *     href="https://unicode-org.github.io/icu-docs/apidoc/dev/icu4j/com/ibm/icu/text/Transliterator.html">https://unicode-org.github.io/icu-docs/apidoc/dev/icu4j/com/ibm/icu/text/Transliterator.html</a>
   */
  public SlugGenerator(String transliteratorId) {
    transliterator = Transliterator.getInstance(transliteratorId);
  }

  /**
   * Set the max length, a slug can have. If unset, its length is unlimited. <br>
   * If limited, then the slug is stripped according to the following receipt:
   *
   * <ul>
   *   <li>If the slug contains no dashes, it is cutted hard at the exact length
   *   <li>If it contains dashes, it is cutted at the last dash before the maximum allowed length
   * </ul>
   *
   * @param maxLength
   */
  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  /**
   * Build a slug for a given string.
   *
   * <p>This is done by replacing all characters outside [A-Za-z0-9] by a dash, by avoiding
   * repeating dashes and dashes at the beginning and at the end. Additionally, special characters
   * like german umlauts are transformed into their base form. Last but not least, the calculated
   * slug is transformed to lowercase
   *
   * @param string as input
   * @return slug in a web-compatible but human readable form
   */
  public String generateSlug(String string) {
    if (string == null || string.isBlank()) {
      return string;
    }

    // Transform umlauts and others
    String slug = transliterator.transliterate(string);
    // All non A-Za-z0-9 characters are replaced by a dash
    slug = slug.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}-]", "-");
    // Repeating dashes are removed
    slug = slug.replaceAll("-+", "-");
    // Remove initial and final dashes
    slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");
    // Transform string to lowercase
    slug = slug.toLowerCase(Locale.ROOT);

    if (maxLength > 0) {
      slug = limitLength(slug);
    }
    return slug;
  }

  /**
   * Check if the given slug is valid.
   *
   * @param slug the slug to check for validity
   * @return indicator if the given slug is valid
   */
  public boolean isValidSlug(String slug) {
    return generateSlug(slug).equalsIgnoreCase(slug);
  }

  /**
   * Check if the given slug is valid when taking care of the case sensitivity.
   *
   * @param slug the slug to check for validity
   * @return indicator if the given slug is valid
   */
  public boolean isValidSlugCaseSensitive(String slug) {
    return generateSlug(slug).equals(slug);
  }

  private String limitLength(String slug) {
    if (slug == null || slug.isEmpty() || slug.length() <= maxLength) {
      return slug;
    }

    // If no dashes are found, we must just cut it to <maxlength>
    if (!slug.contains("-")) {
      return slug.substring(0, maxLength);
    }

    // otherwise first cut to <maxlength>, then go back to the last dash and strip everything from
    // there on
    while (slug.length() > maxLength) {
      slug = slug.substring(0, slug.lastIndexOf("-"));
    }

    return slug;
  }
}
