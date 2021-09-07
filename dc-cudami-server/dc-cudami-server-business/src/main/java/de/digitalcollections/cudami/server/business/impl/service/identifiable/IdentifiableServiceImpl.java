package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("identifiableService")
public class IdentifiableServiceImpl<I extends Identifiable> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  @Autowired private LocaleService localeService;
  @Autowired private UrlAliasService urlAliasService;

  protected IdentifiableRepository<I> repository;

  @Autowired
  public IdentifiableServiceImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository<I> repository) {
    this.repository = repository;
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
  @Transactional(rollbackFor = {RuntimeException.class, IdentifiableServiceException.class})
  public boolean delete(List<UUID> uuids) throws IdentifiableServiceException {
    for (UUID uuid : uuids) {
      try {
        this.urlAliasService.deleteAllForTarget(uuid);
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
  public SearchPageResponse<I> find(SearchPageRequest searchPageRequest) {
    setDefaultSorting(searchPageRequest);
    return repository.find(searchPageRequest);
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    return repository.find(searchTerm, maxResults);
  }

  @Override
  public List<I> findAllFull() {
    return repository.findAllFull();
  }

  @Override
  public List<I> findAllReduced() {
    return repository.findAllReduced();
  }

  @Override
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<I> result = repository.findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public I get(Identifier identifier) {
    return repository.findOne(identifier);
  }

  @Override
  public I get(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public I get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    // get identifiable with all translations:
    I identifiable = get(uuid);
    return reduceMultilanguageFieldsToGivenLocale(identifiable, locale);
  }

  @Override
  public I getByIdentifier(String namespace, String id) {
    return repository.findOneByIdentifier(namespace, id);
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
    // TODO maybe a better solution to just get locale specific fields directly from
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
  @Transactional(rollbackFor = {IdentifiableServiceException.class, RuntimeException.class})
  public I save(I identifiable) throws IdentifiableServiceException {
    try {
      I savedIdentifiable = this.repository.save(identifiable);
      if (identifiable.getLocalizedUrlAliases() != null) {
        LocalizedUrlAliases savedUrlAliases = new LocalizedUrlAliases();
        for (UrlAlias urlAlias : identifiable.getLocalizedUrlAliases().flatten()) {
          // since we have the identifiable's UUID just here
          // the targetUuid must be set at this point
          urlAlias.setTargetUuid(savedIdentifiable.getUuid());
          UrlAlias savedAlias = this.urlAliasService.create(urlAlias);
          savedUrlAliases.add(savedAlias);
        }
        savedIdentifiable.setLocalizedUrlAliases(savedUrlAliases);
      }
      return savedIdentifiable;
    } catch (CudamiServiceException e) {
      LOGGER.error(String.format("Cannot save UrlAliases for: %s", identifiable), e);
      throw new IdentifiableServiceException(e.getMessage());
    } catch (Exception e) {
      LOGGER.error("Cannot save identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID identifiableUuid, List<Entity> entities) {
    return repository.saveRelatedEntities(identifiableUuid, entities);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources) {
    return repository.saveRelatedFileResources(identifiableUuid, fileResources);
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: german label ascending
    // TODO or make dependend from language the user has chosen...?
    if (!pageRequest.hasSorting()) {
      // TODO: discuss default sorting (what if only english label exists? or german and english?)
      String defaultLanguage = localeService.getDefaultLanguage();
      final Order labelOrder1 = new Order(Direction.ASC, "label");
      labelOrder1.setSubProperty(defaultLanguage);
      final Order labelOrder2 = new Order(Direction.ASC, "label");
      labelOrder2.setSubProperty("");
      Sorting sorting = Sorting.defaultBuilder().order(labelOrder1).order(labelOrder2).build();
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  @Transactional(rollbackFor = {RuntimeException.class, IdentifiableServiceException.class})
  public I update(I identifiable) throws IdentifiableServiceException {
    try {
      I updatedIdentifiable = this.repository.update(identifiable);
      // UrlAliases
      this.urlAliasService.deleteAllForTarget(identifiable.getUuid());
      if (identifiable.getLocalizedUrlAliases() != null) {
        LocalizedUrlAliases savedLocalizedUrlAliases = new LocalizedUrlAliases();
        for (UrlAlias urlAlias : identifiable.getLocalizedUrlAliases().flatten()) {
          UrlAlias savedAlias = this.urlAliasService.create(urlAlias);
          savedLocalizedUrlAliases.add(savedAlias);
        }
        updatedIdentifiable.setLocalizedUrlAliases(savedLocalizedUrlAliases);
      }
      return updatedIdentifiable;
    } catch (Exception e) {
      LOGGER.error("Cannot update identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
