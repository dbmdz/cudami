package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguage;
import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguageLocale;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.util.SlugGenerator;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for UrlAlias handling. */
@Service
// @Transactional(rollbackFor = {Exception.class}) //is set on super class
public class UrlAliasServiceImpl extends UniqueObjectServiceImpl<UrlAlias, UrlAliasRepository>
    implements UrlAliasService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlAliasServiceImpl.class);
  private final LocaleService localeService;
  private final SlugGenerator slugGenerator;

  @Autowired
  public UrlAliasServiceImpl(
      UrlAliasRepository repository, SlugGenerator slugGenerator, LocaleService localeService) {
    super(repository);
    this.slugGenerator = slugGenerator;
    this.localeService = localeService;
  }

  protected void checkPublication(UrlAlias urlAlias) throws ServiceException {
    if (urlAlias.getLastPublished() != null) {
      if (urlAlias.getUuid() != null) {
        UrlAlias publishedAlias = getByExample(urlAlias);
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
          throw new ServiceException(
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
  @Transactional(rollbackFor = {RuntimeException.class, ServiceException.class})
  public boolean deleteByIdentifiable(Identifiable identifiable, boolean force)
      throws ServiceException, ConflictException {
    try {
      return repository.deleteByIdentifiable(identifiable, force);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure");
    }
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
  public PageResponse<LocalizedUrlAliases> findLocalizedUrlAliases(PageRequest pageRequest)
      throws ServiceException {
    try {
      return repository.findLocalizedUrlAliases(pageRequest);
    } catch (Exception e) {
      throw new ServiceException(
          "Cannot find LocalizedUrlAliases with pageRequest=" + pageRequest + ": " + e, e);
    }
  }

  @Override
  public String generateSlug(Locale pLocale, String label, Website website)
      throws ServiceException {

    String slug = slugGenerator.generateSlug(label);

    try {
      if (!repository.hasUrlAlias(slug, website, pLocale)) {
        return slug;
      }
    } catch (Exception e) {
      throw new ServiceException(
          "Cannot check, if UrlAliases for website="
              + website
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
        if (!repository.hasUrlAlias(numericalSuffixedSlug, website, pLocale)) {
          return numericalSuffixedSlug;
        }
      } catch (Exception e) {
        throw new ServiceException(
            "Cannot check, if UrlAliases for website="
                + website
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
  public LocalizedUrlAliases getByIdentifiable(Identifiable identifiable) throws ServiceException {
    try {
      return repository.getByIdentifiable(identifiable);
    } catch (Exception e) {
      throw new ServiceException(
          "Cannot find LocalizedUrlAliases for identifiable =" + identifiable + ": " + e, e);
    }
  }

  @Override
  public LocalizedUrlAliases getPrimaryUrlAliases(Website website, String slug, Locale pLocale)
      throws ServiceException {
    if (slug == null || slug.isBlank()) {
      throw new ServiceException("Missing or empty slug");
    }

    try {
      if (website == null) {
        // We only want the unspecified primary links
        LocalizedUrlAliases unspecificLocalizedUrlAliases =
            repository.findPrimaryLinksForWebsite((Website) null, slug, pLocale != null);
        unspecificLocalizedUrlAliases =
            filterForLocaleWithFallback(pLocale, unspecificLocalizedUrlAliases);
        return unspecificLocalizedUrlAliases;
      }

      // Try to retrieve the specific localizedUrlAliases for a website
      LocalizedUrlAliases localizedUrlAliases =
          repository.findPrimaryLinksForWebsite(website, slug, pLocale != null);
      if (localizedUrlAliases.isEmpty()) {
        // Fallback to generic localizedUrlAliases
        localizedUrlAliases =
            repository.findPrimaryLinksForWebsite((Website) null, slug, pLocale != null);
      }

      localizedUrlAliases = filterForLocaleWithFallback(pLocale, localizedUrlAliases);

      return localizedUrlAliases;
    } catch (Exception e) {
      throw new ServiceException(
          "Could not find mainLink for website=" + website + ", slug=" + slug + ": " + e, e);
    }
  }

  @Override
  public List<UrlAlias> getPrimaryUrlAliasesByIdentifiable(Identifiable identifiable)
      throws ServiceException {
    try {
      LocalizedUrlAliases localizedUrlAliases = repository.getByIdentifiable(identifiable);
      if (localizedUrlAliases == null) {
        return new ArrayList<>(0);
      }
      return localizedUrlAliases.flatten().stream()
          .filter(ua -> ua.isPrimary())
          .collect(Collectors.toList());
    } catch (RepositoryException e) {
      throw new ServiceException(
          String.format("Cannot find primary UrlAliases by identifiable='%s'.", identifiable), e);
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
  public void save(UrlAlias urlAlias, boolean force) throws ServiceException {
    if (urlAlias == null) {
      throw new ServiceException("Cannot create an empty UrlAlias");
    }
    // TODO: do not work with uuid in service layer
    if (!force && urlAlias.getUuid() != null) {
      throw new ServiceException("Cannot create an UrlAlias, when its UUID is already set!");
    }

    this.checkPublication(urlAlias);
    try {
      repository.save(urlAlias);
    } catch (Exception e) {
      throw new ServiceException("Cannot save urlAlias: " + e, e);
    }
  }

  @Override
  public void update(UrlAlias urlAlias) throws ServiceException {
    if (urlAlias == null) {
      throw new ServiceException("Cannot update an empty UrlAlias");
    }
    // TODO: do not work with uuid in service layer
    if (urlAlias.getUuid() == null) {
      throw new ServiceException("Cannot update an UrlAlias with empty UUID");
    }

    checkPublication(urlAlias);
    try {
      repository.update(urlAlias);
    } catch (Exception e) {
      throw new ServiceException("Cannot update urlAlias: " + e, e);
    }
  }

  @Override
  public void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException {
    if (localizedUrlAliases == null || localizedUrlAliases.isEmpty()) {
      return;
    }

    Map<String, Integer> primaries = new HashMap<>(0);
    // TODO: do not work with uuid in service layer
    localizedUrlAliases.flatten().stream()
        .forEach(
            u -> {
              String key =
                  (u.getWebsite() != null ? u.getWebsite().getUuid() : "default")
                      + "-"
                      + u.getTarget().getUuid()
                      + "-"
                      + grabLanguage(u.getTargetLanguage());
              Integer primariesPerTuple = primaries.getOrDefault(key, 0);
              if (u.isPrimary()) {
                primariesPerTuple++;
              }
              primaries.put(key, primariesPerTuple);
            });

    // Validation is failed, if we do not have only single primaries per tuple, or
    // if we have no
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
        // TODO: do not work with uuid in service layer
        throw new ValidationException(
            "multiple entries for slug="
                + u.getSlug()
                + ", language="
                + grabLanguage(u.getTargetLanguage())
                + ", website="
                + u.getWebsite()
                + ", target="
                + u.getTarget().getUuid()
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
