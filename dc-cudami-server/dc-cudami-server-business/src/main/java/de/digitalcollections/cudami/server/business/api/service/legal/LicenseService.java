package de.digitalcollections.cudami.server.business.api.service.legal;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.model.legal.License;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/** Service for licence handling. */
public interface LicenseService extends UniqueObjectService<License> {

  /**
   * Delete a license by url
   *
   * @param url unique url of license
   */
  void deleteByUrl(URL url);

  /**
   * Return list of all licenses
   *
   * @return list of all licenses
   */
  List<License> getAll();

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
