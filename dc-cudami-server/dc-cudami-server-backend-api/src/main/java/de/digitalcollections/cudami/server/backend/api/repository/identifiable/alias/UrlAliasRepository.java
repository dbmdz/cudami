package de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public interface UrlAliasRepository extends UniqueObjectRepository<UrlAlias> {

  /**
   * Returns the language of the passed locale w/o anything else e.g. country or script.
   *
   * <p>This is a safe method to always get a language code even if a language is not set at all or
   * {@code Locale.ROOT} is used ("und" is returned). If the locale is {@code null} then "und"
   * (Undetermined) is returned. See also {@link Locale#toLanguageTag()}.
   *
   * @param locale
   * @return
   */
  public static String grabLanguage(Locale locale) {
    if (locale == null) return "und";
    return locale.toLanguageTag().split("-", 2)[0];
  }

  /**
   * Same as {@link #grabLanguage(Locale)}.
   *
   * @param locale
   * @return
   */
  public static Locale grabLanguageLocale(Locale locale) {
    return Locale.forLanguageTag(grabLanguage(locale));
  }

  /**
   * Filter the locales by their script and return only those with
   *
   * <ul>
   *   <li>"" (no script)
   *   <li>"Latn"
   * </ul>
   *
   * @param locales
   * @return
   */
  public static List<Locale> grabLocalesByScript(Collection<Locale> locales) {
    if (locales == null) return Collections.emptyList();
    List<String> scripts = List.of("", "Latn");
    return locales.stream()
        .filter(l -> l != null && scripts.contains(l.getScript()))
        .collect(Collectors.toList());
  }

  default boolean deleteByIdentifiable(Identifiable identifiable, boolean force)
      throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("delete failed: given object must not be null");
    }
    return deleteByIdentifiable(identifiable.getUuid(), force);
  }

  boolean deleteByIdentifiable(UUID identifiableUuid, boolean force) throws RepositoryException;

  /**
   * Retrieve all primary links corresponding to a slug. The owning website is ignored.
   *
   * @param slug the slug to retrieve the primary aliases for
   * @return all {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws RepositoryException
   */
  LocalizedUrlAliases findAllPrimaryLinks(String slug) throws RepositoryException;

  /**
   * Generic request method for getting a parametrized list.
   *
   * @param pageRequest request params for list
   * @return pagable result list
   * @throws RepositoryException
   */
  PageResponse<LocalizedUrlAliases> findLocalizedUrlAliases(PageRequest pageRequest)
      throws RepositoryException;

  /**
   * Retrieve the primary links corresponding to a slug and the target's language.
   *
   * @param websiteUuid the owning website's UUID, can be {@code null} and will be selected as is
   * @param slug the slug to retrieve the primary aliases for
   * @return {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws RepositoryException
   */
  default LocalizedUrlAliases findPrimaryLinksForWebsite(UUID websiteUuid, String slug)
      throws RepositoryException {
    return findPrimaryLinksForWebsite(websiteUuid, slug, true);
  }

  /**
   * Retrieve the primary links corresponding to a slug.
   *
   * @param websiteUuid the owning website's UUID, can be {@code null} and will be selected as is
   * @param slug the slug to retrieve the primary aliases for
   * @param considerLanguage if true consider the language(s) of the target when searching for the
   *     primary links
   * @return {@code UrlAlias}es with {@code isPrimary() == true}
   * @throws RepositoryException
   */
  LocalizedUrlAliases findPrimaryLinksForWebsite(
      UUID websiteUuid, String slug, boolean considerLanguage) throws RepositoryException;

  default LocalizedUrlAliases findPrimaryLinksForWebsite(
      Website website, String slug, boolean considerLanguage) throws RepositoryException {
    UUID websiteUuid = null;
    if (website != null) {
      websiteUuid = website.getUuid();
    }
    return findPrimaryLinksForWebsite(websiteUuid, slug, considerLanguage);
  }

  default LocalizedUrlAliases getByIdentifiable(Identifiable identifiable)
      throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByIdentifiable(identifiable.getUuid());
  }

  /**
   * Retrieve all slugs of a link target.
   *
   * @param uuid the target's (Webpage, Collection,...) UUID
   * @return {@code LocalizedUrlAliases} containing all {@code UrlAlias} objects for that target
   * @throws RepositoryException
   */
  LocalizedUrlAliases getByIdentifiable(UUID uuid) throws RepositoryException;

  /**
   * Check whether an entry exists for the passed website UUID, slug and language.
   *
   * @param slug not null
   * @param websiteUuid can be null
   * @param targetLanguage can be null
   * @return true if slug of website has an url alias for given language
   * @throws RepositoryException
   */
  boolean hasUrlAlias(String slug, UUID websiteUuid, Locale targetLanguage)
      throws RepositoryException;

  default boolean hasUrlAlias(String slug, Website website, Locale targetLanguage)
      throws RepositoryException {
    if (website == null) {
      throw new IllegalArgumentException("check failed: given object must not be null");
    }
    return hasUrlAlias(slug, website.getUuid(), targetLanguage);
  }
}
