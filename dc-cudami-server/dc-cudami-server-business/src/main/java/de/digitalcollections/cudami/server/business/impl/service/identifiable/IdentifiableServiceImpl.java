package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ResourceNotFoundException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("identifiableService")
@Transactional(rollbackFor = {Exception.class})
public class IdentifiableServiceImpl<I extends Identifiable, R extends IdentifiableRepository<I>>
    extends UniqueObjectServiceImpl<I, R> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  private final CudamiConfig cudamiConfig;
  protected IdentifierService identifierService;
  private final LocaleService localeService;

  private final UrlAliasService urlAliasService;

  public IdentifiableServiceImpl(
      @Qualifier("identifiableRepositoryImpl") R repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(repository);
    this.identifierService = identifierService;
    this.urlAliasService = urlAliasService;
    this.localeService = localeService;
    this.cudamiConfig = cudamiConfig;
  }

  @Override
  public void addRelatedEntity(I identifiable, Entity entity) {
    repository.addRelatedEntity(identifiable, entity);
  }

  @Override
  public void addRelatedFileresource(I identifiable, FileResource fileResource) {
    repository.addRelatedFileresource(identifiable, fileResource);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean delete(List<UUID> uuids) throws ServiceException {
    for (UUID uuid : uuids) {
      try {
        deleteIdentifiers(uuid);
      } catch (ServiceException e) {
        throw new ServiceException("Error while removing Identifiers. Rollback.", e);
      }
      try {
        urlAliasService.deleteAllForTarget(uuid, true);
      } catch (ServiceException e) {
        throw new ServiceException("Error while removing UrlAliases. Rollback.", e);
      }
    }
    return repository.delete(uuids);
  }

  private boolean deleteIdentifiers(UUID identifiableUuid) throws ServiceException {
    I identifiable = getByUuid(identifiableUuid);
    if (identifiable == null || identifiable.getIdentifiers() == null) {
      return false;
    }
    identifierService.delete(identifiable.getIdentifiers());
    return true;
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    PageResponse<I> response = repository.find(pageRequest);
    return response;
  }

  @Override
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<I> result = repository.findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public I getByIdentifier(Identifier identifier) {
    return repository.getByIdentifier(identifier);
  }

  @Override
  public I getByIdentifiable(I identifiable) throws ServiceException {
    return repository.getByIdentifiable(identifiable);
  }

  @Override
  public I getByIdentifiableAndLocale(I identifiable, Locale locale) throws ServiceException {
    // getByIdentifier identifiable with all translations:
    identifiable = getByIdentifiable(identifiable);
    return reduceMultilanguageFieldsToGivenLocale(identifiable, locale);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public List<Entity> getRelatedEntities(UUID identifiableUuid) {
    return repository.findRelatedEntities(identifiableUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID identifiableUuid) {
    return repository.findRelatedFileResources(identifiableUuid);
  }

  protected I reduceMultilanguageFieldsToGivenLocale(I identifiable, Locale locale) {
    if (identifiable == null) {
      return null;
    }

    LocalizedText label = identifiable.getLabel();
    if (!label.containsKey(locale) || locale == null) {
      // identifiable does not exist in requested language, so try with default locale
      locale = new Locale(localeService.getDefaultLanguage());
      if (!label.containsKey(locale)) {
        locale = label.getLocales().iterator().next();
      }
    }
    if (locale == null) {
      // an identifiable without label is not allowed/should not be possible (because required!)
      return null;
    }

    final Locale fLocale = locale; // needed final for following lambda expressions

    // filter out all translations not in requested locale
    // TODO maybe a better solution to just getByIdentifier locale specific fields directly from
    // database/repository instead of removing it here?
    // filter label
    label.entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));

    // filter description
    if (identifiable.getDescription() != null) {
      identifiable.getDescription().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    }

    return identifiable;
  }

  @Override
  public void save(I identifiable) throws ServiceException, ValidationException {
    validate(identifiable);
    try {
      repository.save(identifiable);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot save identifiable " + identifiable + ": " + e, e);
    }

    try {
      identifiable.setIdentifiers(
          identifierService.saveForIdentifiable(
              identifiable.getUuid(), identifiable.getIdentifiers()));
    } catch (ServiceException e) {
      LOGGER.error(
          String.format(
              "Cannot save Identifiers %s: %s for %s",
              identifiable.getIdentifiers(), e.getMessage(), identifiable),
          e);
      throw e;
    }

    try {
      IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
          identifiable, cudamiConfig, urlAliasService::generateSlug);
      urlAliasService.validate(identifiable.getLocalizedUrlAliases());

      if (identifiable.getLocalizedUrlAliases() != null
          && !identifiable.getLocalizedUrlAliases().isEmpty()) {
        LocalizedUrlAliases savedUrlAliases = new LocalizedUrlAliases();
        for (UrlAlias urlAlias : identifiable.getLocalizedUrlAliases().flatten()) {
          // since we have the identifiable's UUID just here
          // the targetUuid must be set at this point
          urlAlias.setTargetUuid(identifiable.getUuid());
          urlAliasService.save(urlAlias);
          savedUrlAliases.add(urlAlias);
        }
        identifiable.setLocalizedUrlAliases(savedUrlAliases);
      }
    } catch (ServiceException e) {
      LOGGER.error(String.format("Cannot save UrlAliases for: %s", identifiable), e);
      throw e;
    }
  }

  @Override
  public List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities) {
    return repository.setRelatedEntities(identifiableUuid, entities);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources) {
    return repository.setRelatedFileResources(identifiableUuid, fileResources);
  }

  @Override
  public void update(I identifiable) throws ServiceException, ValidationException {
    validate(identifiable);

    I identifiableInDb = repository.getByUuid(identifiable.getUuid());
    if (identifiableInDb == null) {
      throw new ResourceNotFoundException(
          "No "
              + identifiable.getClass().getSimpleName()
              + " found with uuid="
              + identifiable.getUuid());
    }

    try {
      repository.update(identifiable);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot update identifiable " + identifiable + ": " + e, e);
    }

    try {
      Set<Identifier> existingIdentifiers = identifiableInDb.getIdentifiers();
      Set<Identifier> providedIdentifiers = identifiable.getIdentifiers();
      Set<Identifier> obsoleteIdentifiers =
          existingIdentifiers.stream()
              .filter(i -> !providedIdentifiers.contains(i))
              .collect(Collectors.toSet());
      Set<Identifier> missingIdentifiers =
          providedIdentifiers.stream()
              .filter(i -> !existingIdentifiers.contains(i))
              .collect(Collectors.toSet());

      if (!obsoleteIdentifiers.isEmpty()) {
        identifierService.delete(obsoleteIdentifiers);
      }

      if (!missingIdentifiers.isEmpty()) {
        providedIdentifiers.removeAll(missingIdentifiers);
        Set<Identifier> savedIdentifiers =
            identifierService.saveForIdentifiable(identifiable.getUuid(), missingIdentifiers);
        providedIdentifiers.addAll(savedIdentifiers);
      }
    } catch (ServiceException e) {
      LOGGER.error(
          String.format(
              "Cannot save Identifiers %s: %s for %s",
              identifiable.getIdentifiers(), e.getMessage(), identifiable),
          e);
      throw e;
    }
    try {
      // If we do not want any UrlAliases for this kind of identifiable, we return early
      if (IdentifiableUrlAliasAlignHelper.checkIdentifiableExcluded(identifiable, cudamiConfig)) {
        return;
      }

      // UrlAliases
      IdentifiableUrlAliasAlignHelper.alignForUpdate(
          identifiable, identifiableInDb, cudamiConfig, urlAliasService::generateSlug);
      urlAliasService.deleteAllForTarget(identifiable.getUuid());

      // Validate again, because the default aliases insurance above can alter
      // the data
      try {
        urlAliasService.validate(identifiable.getLocalizedUrlAliases());
      } catch (ValidationException e) {
        throw new ValidationException("Validation error: " + e, e);
      }

      if (identifiable.getLocalizedUrlAliases() != null) {
        for (UrlAlias urlAlias : identifiable.getLocalizedUrlAliases().flatten()) {
          if (urlAlias.getUuid() != null && urlAlias.getLastPublished() != null) {
            // these haven't been removed from DB so we must update them
            urlAliasService.update(urlAlias);
          } else {
            urlAliasService.save(urlAlias, true);
          }
        }
      }
    } catch (ServiceException e) {
      LOGGER.error("Error while updating URL aliases for " + identifiable, e);
      throw e;
    }
  }

  @Override
  public void validate(I identifiable) throws ServiceException, ValidationException {
    if (identifiable.getLabel() == null || identifiable.getLabel().isEmpty()) {
      throw new ValidationException("Missing label");
    }

    try {
      identifierService.validate(identifiable.getIdentifiers());
    } catch (ServiceException e) {
      throw new ValidationException("Cannot validate: " + e, e);
    }
  }
}
