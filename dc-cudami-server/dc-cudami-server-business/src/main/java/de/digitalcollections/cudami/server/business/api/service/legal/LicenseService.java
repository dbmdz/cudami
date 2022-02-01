package de.digitalcollections.cudami.server.business.api.service.legal;

import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/** Service for licence handling. */
public interface LicenseService {

  /**
   * Return count of licenses.
   *
   * @return the count of licenses
   */
  long count();

  /**
   * Delete a license by url
   *
   * @param url unique url of license
   */
  void deleteByUrl(URL url);

  /**
   * Delete a license by UUID
   *
   * @param uuid unique uuid of license
   */
  void deleteByUuid(UUID uuid);

  /**
   * Delete licenses by their UUIDs
   *
   * @param uuids list of uuids of licenses
   */
  void deleteByUuids(List<UUID> uuids);

  /**
   * Return all licenses paged.
   *
   * @param pageRequest the paging parameters
   * @return Paged list of all licenses
   */
  PageResponse<License> find(PageRequest pageRequest);

  /**
   * Return list of all licenses
   *
   * @return list of all licenses
   */
  List<License> findAll();

  /**
   * Return license with url
   *
   * @param url the url of the license
   * @return The found license
   */
  License getByUrl(URL url);

  /**
   * Return license with uuid
   *
   * @param uuid the uuid of the license
   * @return The found license
   */
  License getByUuid(UUID uuid);

  /**
   * Save a license.
   *
   * @param license the license to be saved
   * @return the saved license
   */
  License save(License license);

  /**
   * Update a license.
   *
   * @param license the license to be updated
   * @return the updated license
   */
  License update(License license);
}
