package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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
public class IdentifiableServiceImpl<I extends Identifiable> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  private CudamiConfig cudamiConfig;
  protected IdentifierService identifierService;
  private LocaleService localeService;
  protected IdentifiableRepository<I> repository;
  private UrlAliasService urlAliasService;

  public IdentifiableServiceImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository<I> repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    this.repository = repository;
    this.identifierService = identifierService;
    this.urlAliasService = urlAliasService;
    this.localeService = localeService;
    this.cudamiConfig = cudamiConfig;
  }

  @Override
  public void addRelatedEntity(UUID identifiableUuid, UUID entityUuid) {
    repository.addRelatedEntity(identifiableUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(UUID identifiableUuid, UUID fileResourceUuid) {
    repository.addRelatedFileresource(identifiableUuid, fileResourceUuid);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean delete(List<UUID> uuids) throws IdentifiableServiceException {
    for (UUID uuid : uuids) {
      try {
        identifierService.deleteByIdentifiable(uuid);
      } catch (CudamiServiceException e) {
        throw new IdentifiableServiceException("Error while removing Identifiers. Rollback.", e);
      }
      try {
        urlAliasService.deleteAllForTarget(uuid, true);
      } catch (CudamiServiceException e) {
        throw new IdentifiableServiceException("Error while removing UrlAliases. Rollback.", e);
      }
    }
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return repository.find(pageRequest);
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    return repository.find(searchTerm, maxResults);
  }

  @Override
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<I> result = repository.findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public List<I> getAllFull() {
    return repository.getAllFull();
  }

  @Override
  public List<I> getAllReduced() {
    return repository.getAllReduced();
  }

  @Override
  public I getByIdentifier(Identifier identifier) {
    return repository.getByIdentifier(identifier);
  }

  @Override
  public I getByIdentifier(String namespace, String id) {
    return repository.getByIdentifier(namespace, id);
  }

  @Override
  public I getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public I getByUuidAndLocale(UUID uuid, Locale locale) throws IdentifiableServiceException {
    // getByIdentifier identifiable with all translations:
    I identifiable = getByUuid(uuid);
    return reduceMultilanguageFieldsToGivenLocale(identifiable, locale);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public List<Entity> getRelatedEntities(UUID identifiableUuid) {
    return repository.getRelatedEntities(identifiableUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID identifiableUuid) {
    return repository.getRelatedFileResources(identifiableUuid);
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
  public I save(I identifiable) throws IdentifiableServiceException, ValidationException {
    try {
      identifierService.validate(identifiable.getIdentifiers());
    } catch (CudamiServiceException e) {
      throw new IdentifiableServiceException(e.getMessage());
    }

    I savedIdentifiable;
    try {
      savedIdentifiable = repository.save(identifiable);
    } catch (Exception e) {
      throw new IdentifiableServiceException(
          "Cannot save identifiable " + identifiable + ": " + e, e);
    }

    try {
      savedIdentifiable.setIdentifiers(
          identifierService.saveForIdentifiable(
              savedIdentifiable.getUuid(), identifiable.getIdentifiers()));
    } catch (CudamiServiceException e) {
      LOGGER.error(String.format("Cannot save Identifiers for: %s", identifiable), e);
      throw new IdentifiableServiceException(e.getMessage());
    }

    try {
      savedIdentifiable.setLocalizedUrlAliases(identifiable.getLocalizedUrlAliases());
      IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
          savedIdentifiable, cudamiConfig, urlAliasService::generateSlug);
      urlAliasService.validate(identifiable.getLocalizedUrlAliases());

      if (savedIdentifiable.getLocalizedUrlAliases() != null
          && !savedIdentifiable.getLocalizedUrlAliases().isEmpty()) {
        LocalizedUrlAliases savedUrlAliases = new LocalizedUrlAliases();
        for (UrlAlias urlAlias : savedIdentifiable.getLocalizedUrlAliases().flatten()) {
          // since we have the identifiable's UUID just here
          // the targetUuid must be set at this point
          urlAlias.setTargetUuid(savedIdentifiable.getUuid());
          UrlAlias savedAlias = urlAliasService.save(urlAlias);
          savedUrlAliases.add(savedAlias);
        }
        savedIdentifiable.setLocalizedUrlAliases(savedUrlAliases);
      }
    } catch (CudamiServiceException e) {
      LOGGER.error(String.format("Cannot save UrlAliases for: %s", identifiable), e);
      throw new IdentifiableServiceException(e.getMessage());
    }
    return savedIdentifiable;
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: lastModified descending, uuid
    // ascending
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
      pageRequest.setSorting(sorting);
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
  public I update(I identifiable) throws IdentifiableServiceException, ValidationException {
    try {
      identifierService.validate(identifiable.getIdentifiers());
    } catch (CudamiServiceException e) {
      throw new IdentifiableServiceException(e.getMessage());
    }
    I identifiableInDb = repository.getByUuid(identifiable.getUuid());

    try {
      repository.update(identifiable);
    } catch (Exception e) {
      throw new IdentifiableServiceException(
          "Cannot update identifiable " + identifiable + ": " + e, e);
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
        identifierService.saveForIdentifiable(identifiable.getUuid(), missingIdentifiers);
      }
    } catch (CudamiServiceException e) {
      LOGGER.error("Error while updating Identifiers for " + identifiable, e);
      throw new IdentifiableServiceException(e.getMessage());
    }
    try {
      // If we do not want any UrlAliases for this kind of identifiable, we return early
      if (IdentifiableUrlAliasAlignHelper.checkIdentifiableExcluded(identifiable, cudamiConfig)) {
        return repository.getByUuid(identifiable.getUuid());
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
    } catch (CudamiServiceException e) {
      LOGGER.error("Error while updating URL aliases for " + identifiable, e);
      throw new IdentifiableServiceException(e.getMessage(), e);
    }

    return repository.getByUuid(identifiable.getUuid());
  }
}
