package de.digitalcollections.cudami.server.business.impl.service.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.FamilyNameRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.agent.FamilyName;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FamilyNameServiceImpl extends IdentifiableServiceImpl<FamilyName>
    implements FamilyNameService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNameServiceImpl.class);

  @Autowired
  public FamilyNameServiceImpl(FamilyNameRepository repository) {
    super(repository);
  }

  @Override
  public PageResponse<FamilyName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<FamilyName> result =
        ((FamilyNameRepository) repository)
            .findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }
}
