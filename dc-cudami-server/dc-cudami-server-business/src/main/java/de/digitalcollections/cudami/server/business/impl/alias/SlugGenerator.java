package de.digitalcollections.cudami.server.business.impl.alias;

import com.ibm.icu.text.Transliterator;
import de.digitalcollections.cudami.server.backend.api.repository.alias.UrlAliasRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SlugGenerator {

  private final UrlAliasRepository urlAliasRepository;

  private final Transliterator transliterator;

  @Autowired
  public SlugGenerator(UrlAliasRepository urlAliasRepository) {
    this.urlAliasRepository = urlAliasRepository;
    transliterator = Transliterator.getInstance("de-ASCII");
  }

  /**
   * Build a slug for a given string.
   *
   * <p>This is done by replacing all characters outside [A-Za-z0-9] by a dash, by avoiding
   * repeating dashes and dashes at the beginning and at the end. Additionally, special characters
   * like german umlauts are transformed into their base form. Last but not least, the calculated
   * slug is transformed to lowercase
   *
   * @param string
   * @return slug
   */
  public String generateSlug(String string) {
    if (!StringUtils.hasText(string)) {
      return string;
    }

    // Transform umlauts and others
    String slug = transliterator.transliterate(string);
    // All non A-Za-z0-9 characters are replaced by a dash
    slug = slug.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}-]", "-");
    // Repeating dashes are removed
    slug = slug.replaceAll("--", "-");
    // Remove initial and final dashes
    slug = slug.replaceAll("^-", "").replaceAll("-$", "");
    // Transform string to lowercase
    slug = slug.toLowerCase(Locale.ROOT);
    return slug;
  }
}
