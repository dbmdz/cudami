package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguage;
import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguageLocale;

import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for UrlAlias handling. */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UrlAliasServiceImpl implements UrlAliasService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlAliasServiceImpl.class);
  private final LocaleService localeService;
  private final UrlAliasRepository repository;
  private final SlugGenerator slugGenerator;

  @Autowired
  public UrlAliasServiceImpl(
      UrlAliasRepository repository, SlugGenerator slugGenerator, LocaleService localeService) {
    this.repository = repository;
    this.slugGenerator = slugGenerator;
    this.localeService = localeService;
  }

  protected void checkPublication(UrlAlias urlAlias) throws CudamiServiceException {
    if (urlAlias.getLastPublished() != null) {
      if (urlAlias.getUuid() != null) {
        UrlAlias publishedAlias = getByUuid(urlAlias.getUuid());
        if (!publishedAlias.isPrimary() && urlAlias.isPrimary()) {
          // set lastPublished to current date
          urlAlias.setLastPublished(LocalDateTime.now());
        }
        // Only the primary flag and lastPublished are permitted to be changed
        // so we sync these two objects and compare them
        publishedAlias.setPrimary(urlAlias.isPrimary());
        publishedAlias.setLastPublished(urlAlias.getLastPublished());
        if (!urlAlias.equals(publishedAlias)) {
          // there are more changes than permitted
          throw new CudamiServiceException(
              String.format(
                  "Error: Attempt to change an already published Alias. UUID: %s",
                  urlAlias.getUuid()));
        }
      }
    } else if (urlAlias.isPrimary()) {
      // no publishing date yet
      urlAlias.setLastPublished(LocalDateTime.now());
    }
  }

  @Override
  public boolean delete(List<UUID> uuids) throws CudamiServiceException {
    try {
      return repository.delete(uuids) > 0;
    } catch (UrlAliasRepositoryException e) {
      throw new CudamiServiceException("Cannot delete UrlAliases by uuids: " + e, e);
    }
  }

  @Override
  @Transactional(rollbackFor = {RuntimeException.class, CudamiServiceException.class})
  public boolean deleteAllForTarget(UUID uuid, boolean force) throws CudamiServiceException {
    if (uuid == null) {
      return false;
    }
    LocalizedUrlAliases urlAliases = getLocalizedUrlAliases(uuid);
    return delete(
        urlAliases.flatten().stream()
            .filter(ua -> force || ua.getLastPublished() == null)
            .map(ua -> ua.getUuid())
            .collect(Collectors.toList()));
  }

  protected LocalizedUrlAliases filterForLocale(
      Locale pLocale, LocalizedUrlAliases localizedUrlAliases) {
    final List<UrlAlias> filteredUrlAliases =
        localizedUrlAliases.flatten().stream()
            .filter(u -> pLocale.equals(grabLanguageLocale(u.getTargetLanguage())))
            .collect(Collectors.toList());
    return new LocalizedUrlAliases(filteredUrlAliases);
  }

  protected LocalizedUrlAliases filterForLocaleWithFallback(
      Locale pLocale, LocalizedUrlAliases localizedUrlAliases) {
    if (pLocale == null) {
      return localizedUrlAliases;
    }

    if (localizedUrlAliases == null || localizedUrlAliases.isEmpty()) {
      return localizedUrlAliases;
    }

    pLocale = grabLanguageLocale(pLocale);
    if (localizedUrlAliases.hasTargetLanguage(pLocale)) {
      // Remove all languages, which do not match the desired pLocale
      return filterForLocale(pLocale, localizedUrlAliases);
    }
    // Remove all languages, which are not the default language
    return filterForLocale(
        Locale.forLanguageTag(localeService.getDefaultLanguage()), localizedUrlAliases);
  }

  @Override
  public PageResponse<LocalizedUrlAliases> find(PageRequest pageRequest)
      throws CudamiServiceException {
    try {
      return repository.find(pageRequest);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Cannot find LocalizedUrlAliases with pageRequest=" + pageRequest + ": " + e, e);
    }
  }

  @Override
  public String generateSlug(Locale pLocale, String label, UUID websiteUuid)
      throws CudamiServiceException {

    String slug = slugGenerator.generateSlug(label);

    try {
      if (!repository.hasUrlAlias(slug, websiteUuid, pLocale)) {
        return slug;
      }
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Cannot check, if UrlAliases for websiteUuid="
              + websiteUuid
              + ", slug="
              + slug
              + ", locale="
              + pLocale
              + " already exist: "
              + e,
          e);
    }

    int suffixId = 1;
    while (true) {
      String numericalSuffixedSlug = slug + "-" + suffixId;
      try {
        if (!repository.hasUrlAlias(numericalSuffixedSlug, websiteUuid, pLocale)) {
          return numericalSuffixedSlug;
        }
      } catch (Exception e) {
        throw new CudamiServiceException(
            "Cannot check, if UrlAliases for websiteUuid="
                + websiteUuid
                + ", slug="
                + numericalSuffixedSlug
                + ", locale="
                + pLocale
                + " already exist: "
                + e,
            e);
      }
      suffixId++;
    }
  }

  @Override
  public UrlAlias getByUuid(UUID uuid) throws CudamiServiceException {
    if (uuid == null) {
      return null;
    }

    try {
      return repository.getByUuid(uuid);
    } catch (Exception e) {
      throw new CudamiServiceException("Cannot find an UrlAlias with uuid=" + uuid + ": " + e, e);
    }
  }

  @Override
  public LocalizedUrlAliases getLocalizedUrlAliases(UUID targetUuid) throws CudamiServiceException {
    try {
      return repository.getAllForTarget(targetUuid);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Cannot find LocalizedUrlAliases for identifiable with uuid=" + targetUuid + ": " + e, e);
    }
  }

  @Override
  public LocalizedUrlAliases getPrimaryUrlAliases(UUID websiteUuid, String slug, Locale pLocale)
      throws CudamiServiceException {
    if (slug == null || slug.isBlank()) {
      throw new CudamiServiceException("Missing or empty slug");
    }

    try {
      if (websiteUuid == null) {
        // We only want the unspecified primary links
        LocalizedUrlAliases unspecificLocalizedUrlAliases =
            repository.findPrimaryLinksForWebsite(null, slug, pLocale != null);
        unspecificLocalizedUrlAliases =
            filterForLocaleWithFallback(pLocale, unspecificLocalizedUrlAliases);
        return unspecificLocalizedUrlAliases;
      }

      // Try to retrieve the specific localizedUrlAliases for a website
      LocalizedUrlAliases localizedUrlAliases =
          repository.findPrimaryLinksForWebsite(websiteUuid, slug, pLocale != null);
      if (localizedUrlAliases.isEmpty()) {
        // Fallback to generic localizedUrlAliases
        localizedUrlAliases = repository.findPrimaryLinksForWebsite(null, slug, pLocale != null);
      }

      localizedUrlAliases = filterForLocaleWithFallback(pLocale, localizedUrlAliases);

      return localizedUrlAliases;
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Could not find mainLink for websiteUuid=" + websiteUuid + ", slug=" + slug + ": " + e,
          e);
    }
  }

  @Override
  public List<UrlAlias> getPrimaryUrlAliasesForTarget(UUID targetUuid)
      throws CudamiServiceException {
    try {
      LocalizedUrlAliases localizedUrlAliases = repository.getAllForTarget(targetUuid);
      if (localizedUrlAliases == null) {
        return new ArrayList<>(0);
      }
      return localizedUrlAliases.flatten().stream()
          .filter(ua -> ua.isPrimary())
          .collect(Collectors.toList());
    } catch (UrlAliasRepositoryException e) {
      throw new CudamiServiceException(
          String.format(
              "Cannot find all UrlAliases of a specific target. targetUuid='%s'.", targetUuid),
          e);
    }
  }

  protected LocalizedUrlAliases removeNonmatchingLanguagesForSlug(
      LocalizedUrlAliases localizedUrlAliases, String slug) {
    List<Locale> matchingLocales =
        localizedUrlAliases.flatten().stream()
            .filter(u -> slug.equalsIgnoreCase(u.getSlug()))
            .map(u -> u.getTargetLanguage())
            .collect(Collectors.toList());
    return new LocalizedUrlAliases(
        localizedUrlAliases.flatten().stream()
            .filter(u -> matchingLocales.contains(u.getTargetLanguage()))
            .collect(Collectors.toList()));
  }

  @Override
  public UrlAlias save(UrlAlias urlAlias, boolean force) throws CudamiServiceException {
    if (urlAlias == null) {
      throw new CudamiServiceException("Cannot create an empty UrlAlias");
    }
    if (!force && urlAlias.getUuid() != null) {
      throw new CudamiServiceException("Cannot create an UrlAlias, when its UUID is already set!");
    }

    this.checkPublication(urlAlias);
    try {
      return repository.save(urlAlias);
    } catch (Exception e) {
      throw new CudamiServiceException("Cannot save urlAlias: " + e, e);
    }
  }

  @Override
  public UrlAlias update(UrlAlias urlAlias) throws CudamiServiceException {
    if (urlAlias == null) {
      throw new CudamiServiceException("Cannot update an empty UrlAlias");
    }
    if (urlAlias.getUuid() == null) {
      throw new CudamiServiceException("Cannot update an UrlAlias with empty UUID");
    }

    checkPublication(urlAlias);
    try {
      return repository.update(urlAlias);
    } catch (Exception e) {
      throw new CudamiServiceException("Cannot update urlAlias: " + e, e);
    }
  }

  @Override
  public void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException {
    if (localizedUrlAliases == null || localizedUrlAliases.isEmpty()) {
      return;
    }

    Map<String, Integer> primaries = new HashMap<>(0);
    localizedUrlAliases.flatten().stream()
        .forEach(
            u -> {
              String key =
                  (u.getWebsite() != null ? u.getWebsite().getUuid() : "default")
                      + "-"
                      + u.getTargetUuid()
                      + "-"
                      + grabLanguage(u.getTargetLanguage());
              Integer primariesPerTuple = primaries.getOrDefault(key, 0);
              if (u.isPrimary()) {
                primariesPerTuple++;
              }
              primaries.put(key, primariesPerTuple);
            });

    // Validation is failed, if we do not have only single primaries per tuple, or if we have no
    // single primary per tuple at all
    if (primaries.values().stream().collect(Collectors.toSet()).size() != 1
        || !primaries.values().contains(1)) {
      throw new ValidationException("violated single primaries: " + primaries);
    }

    // Reject multiple entries for the same slug and (website,target,language) tuple
    Set<String> tuples = new HashSet<>(0);
    for (UrlAlias u : localizedUrlAliases.flatten()) {
      String key =
          (u.getWebsite() != null ? u.getWebsite().getUuid() : "default")
              + "-"
              + grabLanguage(u.getTargetLanguage())
              + "-"
              + u.getSlug();
      if (tuples.contains(key)) {
        throw new ValidationException(
            "multiple entries for slug="
                + u.getSlug()
                + ", language="
                + grabLanguage(u.getTargetLanguage())
                + ", website="
                + u.getWebsite()
                + ", target="
                + u.getTargetUuid()
                + " in "
                + tuples
                + " of "
                + localizedUrlAliases);
      } else {
        tuples.add(key);
      }
    }
  }
}
