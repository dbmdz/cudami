package de.digitalcollections.cudami.server.backend.api.repository.legal;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.legal.License;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/** Repository for licences persistence handling. */
public interface LicenseRepository extends UniqueObjectRepository<License> {

  /**
   * Delete a license by url
   *
   * @param url unique url of license
   */
  void deleteByUrl(URL url);

  /**
   * Return license with url
   *
   * @param url the url of the license
   * @return The found license
   */
  License getByUrl(URL url);

  /**
   * Return list of languages of all licenses
   *
   * @return list of languages
   */
  List<Locale> getLanguages();
}
