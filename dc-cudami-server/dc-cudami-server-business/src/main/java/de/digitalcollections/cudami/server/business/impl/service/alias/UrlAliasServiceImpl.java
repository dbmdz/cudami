package de.digitalcollections.cudami.server.business.impl.service.alias;

import de.digitalcollections.cudami.server.backend.api.repository.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.alias.SlugGenerator;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service implementation for UrlAlias handling. */
@Service
public class UrlAliasServiceImpl implements UrlAliasService {

  private final SlugGenerator slugGenerator;
  private final UrlAliasRepository repository;

  @Value("${cudami.defaults.locale}")
  private Locale defaultLocale;

  @Value("${cudami.urlalias.website.default.uuid}")
  private UUID defaultWebsiteUuid;

  @Autowired
  public UrlAliasServiceImpl(UrlAliasRepository repository, SlugGenerator slugGenerator) {
    this.repository = repository;
    this.slugGenerator = slugGenerator;
  }

  protected void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  protected void setDefaultWebsiteUuid(UUID defaultWebsiteUuid) {
    this.defaultWebsiteUuid = defaultWebsiteUuid;
  }

  @Override
  public UrlAlias findOne(UUID uuid) throws CudamiServiceException {
    if (uuid == null) {
      return null;
    }

    try {
      return repository.findOne(uuid);
    } catch (Exception e) {
      throw new CudamiServiceException("Cannot findOne with uuid=" + uuid + ": " + e, e);
    }
  }

  @Override
  public boolean delete(UUID uuid) throws CudamiServiceException {
    return delete(List.of(uuid));
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
  public UrlAlias create(UrlAlias urlAlias) throws CudamiServiceException {
    if (urlAlias == null) {
      throw new CudamiServiceException("Cannot create an empty UrlAlias");
    }

    if (urlAlias.getUuid() != null) {
      throw new CudamiServiceException("Cannot create an UrlAlias, when its UUID is already set!");
    }

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

    try {
      return repository.update(urlAlias);
    } catch (Exception e) {
      throw new CudamiServiceException("Cannot update urlAlias: " + e, e);
    }
  }

  @Override
  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws CudamiServiceException {
    try {
      return repository.find(searchPageRequest);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Cannot find LocalizedUrlAliases with searchPageRequest=" + searchPageRequest + ": " + e,
          e);
    }
  }

  @Override
  public LocalizedUrlAliases findLocalizedUrlAliases(UUID uuid) throws CudamiServiceException {
    try {
      return repository.findAllForTarget(uuid);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Cannot find LocalizedUrlAliases for identifiable with uuid=" + uuid + ": " + e, e);
    }
  }

  @Override
  public LocalizedUrlAliases findMainLink(UUID websiteUuid, String slug)
      throws CudamiServiceException {
    if (websiteUuid == null) {
      throw new CudamiServiceException("Missing websiteUuid");
    }
    if (slug == null || slug.isBlank()) {
      throw new CudamiServiceException("Missing or empty slug");
    }

    try {
      return repository.findMainLinks(websiteUuid, slug);
    } catch (Exception e) {
      throw new CudamiServiceException(
          "Could not find mainLink for websiteUuid=" + websiteUuid + ", slug=" + slug + ": " + e,
          e);
    }
  }

  @Override
  public String generateSlug(Locale pLocale, String label, UUID websiteUuid)
      throws CudamiServiceException {
    if (websiteUuid == null) {
      websiteUuid = defaultWebsiteUuid;
    }

    String slug = slugGenerator.generateSlug(label);

    try {
      if (!repository.hasUrlAlias(websiteUuid, slug)) {
        return slug;
      }
    } catch (UrlAliasRepositoryException e) {
      throw new CudamiServiceException(
          "Cannot check, if UrlAliase for websiteUuid="
              + websiteUuid
              + ", slug="
              + slug
              + " already exists: "
              + e,
          e);
    }

    // If the provided locale is not the default locale, we start by adding
    // locale-bases suffixes, d.h. "-en".
    if (!pLocale.equals(defaultLocale)) {
      String languageSuffixedSlug = slug + "-" + pLocale.getLanguage();
      try {
        if (!repository.hasUrlAlias(websiteUuid, languageSuffixedSlug)) {
          return languageSuffixedSlug;
        }
      } catch (UrlAliasRepositoryException e) {
        throw new CudamiServiceException(
            "Cannot check, if UrlAliase for websiteUuid="
                + websiteUuid
                + ", slug="
                + languageSuffixedSlug
                + " already exists: "
                + e,
            e);
      }
    }

    // Our last chance is now to add a numerical suffix
    int suffixId = 1;
    while (true) {
      String numericalSuffixedSlug = slug + "-" + suffixId;
      try {
        if (!repository.hasUrlAlias(websiteUuid, numericalSuffixedSlug))
          return numericalSuffixedSlug;
      } catch (UrlAliasRepositoryException e) {
        throw new CudamiServiceException(
            "Cannot check, if UrlAliase for websiteUuid="
                + websiteUuid
                + ", slug="
                + numericalSuffixedSlug
                + " already exists: "
                + e,
            e);
      }
      suffixId++;
    }
  }
}
