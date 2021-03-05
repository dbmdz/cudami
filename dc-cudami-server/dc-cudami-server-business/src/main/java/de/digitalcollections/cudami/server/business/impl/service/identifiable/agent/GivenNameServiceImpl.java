package de.digitalcollections.cudami.server.business.impl.service.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.GivenNameRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GivenNameServiceImpl extends IdentifiableServiceImpl<GivenName>
    implements GivenNameService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GivenNameServiceImpl.class);

  @Autowired
  public GivenNameServiceImpl(GivenNameRepository repository) {
    super(repository);
  }

  @Override
  public PageResponse<GivenName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<GivenName> result =
        ((GivenNameRepository) repository).findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }
}
