package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class IdentifiableServiceImpl<I extends Identifiable> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  protected IdentifiableRepository<I> repository;

  @Autowired
  public IdentifiableServiceImpl(@Qualifier("identifiableRepositoryImpl") IdentifiableRepository<I> repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    return repository.find(searchTerm, maxResults);
  }

  @Override
  public I get(UUID uuid) {
    return (I) repository.findOne(uuid);
  }

  @Override
  public I get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    // get identifiable with all translations:
    I identifiable = get(uuid);
    if (identifiable == null) {
      return null;
    }

    Set<Translation> translations = identifiable.getLabel().getTranslations();
    if (!translationExist(translations, locale) || locale == null) {
      // identifiable does not exist in requested language, so try with default locale
      locale = localeService.getDefault();
      if (!translationExist(translations, locale)) {
        // just use first existing locale
        Optional<Translation> translation = translations.stream().findFirst();
        locale = translation.map(Translation::getLocale).orElse(null);
      }
    }
    if (locale == null) {
      // an identifiable without label is not allowed/should not be possible (because required!)
      return null;
    }

    final Locale fLocale = locale; // needed final for following lambda expressions

    // filter out all translations not in requested locale
    // TODO maybe a better solution to just get locale specific fields directly from database/repository instead of removing it here?
    // filter label
    translations.removeIf((Translation translation) -> !translation.getLocale().equals(fLocale));

    // filter description
    if (identifiable.getDescription() != null && identifiable.getDescription().getLocalizedStructuredContent() != null) {
      identifiable.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    }

    return identifiable;
  }

  private boolean translationExist(Set<Translation> translations, Locale locale) {
    return translations.stream().anyMatch(translation -> translation.getLocale().equals(locale));
  }

  @Override
  //  @Transactional(readOnly = false)
  public I save(I identifiable) throws IdentifiableServiceException {
    try {
      return (I) repository.save(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot save identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public I update(I identifiable) throws IdentifiableServiceException {
    try {
      return (I) repository.update(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot update identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
