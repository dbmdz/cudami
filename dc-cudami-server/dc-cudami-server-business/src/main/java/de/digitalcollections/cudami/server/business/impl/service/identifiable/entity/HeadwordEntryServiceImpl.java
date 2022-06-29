package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.HeadwordEntryRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.HeadwordEntryService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for HeadwordEntry handling. */
@Service
public class HeadwordEntryServiceImpl extends EntityServiceImpl<HeadwordEntry>
    implements HeadwordEntryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordEntryServiceImpl.class);

  public HeadwordEntryServiceImpl(
      HeadwordEntryRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public List<HeadwordEntry> getByHeadword(UUID headwordUuid) {
    return ((HeadwordEntryRepository) repository).getByHeadword(headwordUuid);
  }

  @Override
  public HeadwordEntry getByUuidAndLocale(UUID uuid, Locale locale)
      throws IdentifiableServiceException {
    HeadwordEntry headwordEntry = super.getByUuidAndLocale(uuid, locale);
    if (headwordEntry == null) {
      return null;
    }

    // filter out not requested translations of fields not already filtered
    if (headwordEntry.getText() != null) {
      headwordEntry
          .getText()
          .entrySet()
          .removeIf((Map.Entry entry) -> !entry.getKey().equals(locale));
    }
    return headwordEntry;
  }

  @Override
  public List<Agent> getCreators(UUID headwordEntryUuid) {
    return ((HeadwordEntryRepository) repository).getCreators(headwordEntryUuid);
  }
}
