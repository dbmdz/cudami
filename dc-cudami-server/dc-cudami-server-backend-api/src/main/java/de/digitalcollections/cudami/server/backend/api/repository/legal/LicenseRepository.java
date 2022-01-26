package de.digitalcollections.cudami.server.backend.api.repository.legal;

import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/** Repository for licences persistence handling. */
public interface LicenseRepository {

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
  void delete(String url);

  /**
   * Delete a license by UUID
   *
   * @param uuid unique uuid of license
   */
  void delete(UUID uuid);

  /**
   * Delete licenses by their UUIDs
   *
   * @param uuids list of uuids of licenses
   */
  void delete(List<UUID> uuids);

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
   * Return license with uuid
   *
   * @param uuid the uuid of the license
   * @return The found license
   */
  License findOne(UUID uuid);

  /**
   * Return license with url
   *
   * @param url the url of the license
   * @return The found license
   */
  License findOneByUrl(URL url);

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
