package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
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
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
  public void addRelatedEntity(I identifiable, Entity entity) throws ServiceException {
    try {
      repository.addRelatedEntity(identifiable, entity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void addRelatedFileresource(I identifiable, FileResource fileResource)
      throws ServiceException {
    try {
      repository.addRelatedFileresource(identifiable, fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) throws ServiceException {
    setDefaultSorting(pageRequest);
    try {
      return super.find(pageRequest);
    } catch (ServiceException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws ServiceException {
    try {
      return repository.findByLanguageAndInitial(pageRequest, language, initial);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(I identifiable, PageRequest pageRequest)
      throws ServiceException {
    try {
      return repository.findRelatedEntities(identifiable, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      I identifiable, PageRequest pageRequest) throws ServiceException {
    try {
      return repository.findRelatedFileResources(identifiable, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public I getByExampleAndLocale(I identifiable, Locale locale) throws ServiceException {
    identifiable = getByExample(identifiable);
    return reduceMultilanguageFieldsToGivenLocale(identifiable, locale);
  }

  @Override
  public I getByIdentifier(Identifier identifier) throws ServiceException {
    try {
      return repository.getByIdentifier(identifier);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguages() throws ServiceException {
    try {
      return repository.getLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
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
          identifierService.saveForIdentifiable(identifiable, identifiable.getIdentifiers()));
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
  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: label ascending
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(new Order(Direction.ASC, "label"));
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public List<Entity> setRelatedEntities(I identifiable, List<Entity> entities)
      throws ServiceException {
    try {
      return repository.setRelatedEntities(identifiable, entities);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      I identifiable, List<FileResource> fileResources) throws ServiceException {
    try {
      return repository.setRelatedFileResources(identifiable, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void update(I identifiable) throws ServiceException, ValidationException {
    validate(identifiable);

    I identifiableInDb;
    try {
      identifiableInDb = repository.getByUuid(identifiable.getUuid());
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
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
        try {
          identifierService.delete(obsoleteIdentifiers);
        } catch (ConflictException e) {
          throw new ServiceException("Can not delete obsolete identifiers", e);
        }
      }

      if (!missingIdentifiers.isEmpty()) {
        providedIdentifiers.removeAll(missingIdentifiers);
        Set<Identifier> savedIdentifiers =
            identifierService.saveForIdentifiable(identifiable, missingIdentifiers);
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
      try {
        urlAliasService.deleteByIdentifiable(identifiable);
      } catch (ConflictException e) {
        throw new ServiceException("Can not delete url alias by identifiable", e);
      }

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
