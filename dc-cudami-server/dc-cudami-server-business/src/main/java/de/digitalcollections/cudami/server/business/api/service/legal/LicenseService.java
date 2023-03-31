package de.digitalcollections.cudami.server.business.api.service.legal;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.legal.License;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Service for licence handling. */
public interface LicenseService extends UniqueObjectService<License> {

  /**
   * Delete a license by url
   *
   * @param url unique url of license
   * @throws ServiceException
   */
  void deleteByUrl(URL url) throws ServiceException;

  /**
   * Return list of all licenses
   *
   * @return list of all licenses
   */
  Set<License> getAll() throws ServiceException;

  /**
   * Return license with url
   *
   * @param url the url of the license
   * @return The found license
   */
  License getByUrl(URL url) throws ServiceException;

  /**
   * Return list of languages of all licenses
   *
   * @return list of languages
   */
  List<Locale> getLanguages() throws ServiceException;
}
