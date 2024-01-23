package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ManifestationServiceImpl extends EntityServiceImpl<Manifestation>
    implements ManifestationService {

  private EntityToEntityRelationService entityRelationService;

  public ManifestationServiceImpl(
      ManifestationRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      EntityToEntityRelationService entityRealationService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
    this.entityRelationService = entityRealationService;
  }

  @Override
  public PageResponse<Manifestation> findManifestationsByWork(Work work, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((ManifestationRepository) repository).findManifestationsByWork(work, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot retrieve manifestations for work with uuid=" + work + ": " + e, e);
    }
  }

  @Override
  public PageResponse<Manifestation> findSubParts(
      Manifestation manifestation, PageRequest pageRequest) throws ServiceException {
    try {
      return ((ManifestationRepository) repository).findSubParts(manifestation, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfManifestationsForWork(Work work) throws ServiceException {
    try {
      return ((ManifestationRepository) repository).getLanguagesOfManifestationsForWork(work);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeParent(Manifestation manifestation, Manifestation parentManifestation)
      throws ServiceException {
    try {
      return ((ManifestationRepository) repository)
          .removeParent(manifestation, parentManifestation);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(Manifestation manifestation) throws ServiceException, ValidationException {
    super.save(manifestation);
    try {
      List<EntityRelation> entityRelations = manifestation.getRelations();
      entityRelationService.setEntityRelations(manifestation, entityRelations, true);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot save Manifestation=" + manifestation + ": " + e, e);
    }
  }

  @Override
  public void update(Manifestation manifestation) throws ServiceException, ValidationException {
    super.update(manifestation);
    try {
      List<EntityRelation> entityRelations = manifestation.getRelations();
      entityRelationService.setEntityRelations(manifestation, entityRelations, false);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot update Manifestation=" + manifestation + ": " + e, e);
    }
  }
}
