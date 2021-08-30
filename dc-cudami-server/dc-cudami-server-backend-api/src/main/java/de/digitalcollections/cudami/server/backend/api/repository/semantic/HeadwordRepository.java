package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Headwords handling */
public interface HeadwordRepository {

  long count();

  /**
   * Delete a headword.
   *
   * @param label label of headword
   * @param locale locale of label
   */
  void delete(String label, Locale locale);

  void delete(UUID uuid);

  /**
   * Return paged list of headwords
   *
   * @param pageRequest request for page
   * @return page response
   */
  PageResponse<Headword> find(PageRequest pageRequest);

  /**
   * Return all headwords
   *
   * @return List of all headwords
   */
  List<Headword> findAll();

  /**
   * Returns a list of headwords, if available
   *
   * @param label label of headword, e.g. "München" (locale ignored)
   * @return list of headwords or null
   */
  List<Headword> findByLabel(String label);

  Headword findOne(UUID uuid);

  /**
   * Returns a headword, if available
   *
   * @param label label of headword, e.g. "München"
   * @param locale locale of label, e.g. "de"
   * @return Headword or null
   */
  Headword findOneByLabelAndLocale(String label, Locale locale);

  /**
   * Save a Headword.
   *
   * @param headword the headword to be saved
   * @return the saved headword with updated timestamps
   */
  Headword save(Headword headword);
}
