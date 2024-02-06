package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class WorkServiceImpl extends EntityServiceImpl<Work> implements WorkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkServiceImpl.class);
  private final EntityToEntityRelationService entityRelationService;

  public WorkServiceImpl(
      @Qualifier("workRepository") WorkRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      EntityToEntityRelationService entityRelationService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
    this.entityRelationService = entityRelationService;
  }

  @Override
  public PageResponse<Work> findEmbeddedWorks(Work work, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((WorkRepository) repository).findEmbeddedWorks(work, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Work getByItem(Item item) throws ServiceException {
    try {
      return ((WorkRepository) repository).getByItem(item);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Set<Work> getByPerson(Person person) throws ServiceException {
    try {
      return ((WorkRepository) repository).getByPerson(person);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(Work work) throws ServiceException, ValidationException {
    super.save(work);
    try {
      List<EntityRelation> entityRelations = work.getRelations();
      entityRelationService.setEntityRelations(work, entityRelations, true);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot save Work=" + work + ": " + e, e);
    }
  }

  @Override
  public void update(Work work) throws ServiceException, ValidationException {
    super.update(work);
    try {
      List<EntityRelation> entityRelations = work.getRelations();
      entityRelationService.setEntityRelations(work, entityRelations, false);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot update Work=" + work + ": " + e, e);
    }
  }
}
