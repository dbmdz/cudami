package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.paging.OrderImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class IdentifiableServiceImpl<I extends Identifiable> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  @Autowired private LocaleService localeService;

  protected IdentifiableRepository<I> repository;

  @Autowired
  public IdentifiableServiceImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository<I> repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean delete(List<UUID> uuids) {
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
  //  @Transactional(readOnly = false)
  public I save(I identifiable) throws IdentifiableServiceException {
    try {
      return repository.save(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot save identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: german label ascending
    // TODO or make dependend from language the user has chosen...?
    if (pageRequest.getSorting() == null
        || pageRequest.getSorting().getOrders() == null
        || pageRequest.getSorting().getOrders().isEmpty()) {
      // TODO: discuss default sorting (what if only english label exists? or german and english?)
      final OrderImpl labelOrder1 = new OrderImpl(Direction.ASC, "label");
      labelOrder1.setSubProperty("de");
      final OrderImpl labelOrder2 = new OrderImpl(Direction.ASC, "label");
      labelOrder2.setSubProperty("");
      Sorting sorting = Sorting.defaultBuilder().order(labelOrder1).order(labelOrder2).build();
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public I update(I identifiable) throws IdentifiableServiceException {
    try {
      return repository.update(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot update identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
