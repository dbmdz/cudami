package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ManifestationServiceImpl extends EntityServiceImpl<Manifestation>
    implements ManifestationService {

  private EntityRelationService entityRelationService;

  public ManifestationServiceImpl(
      ManifestationRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      EntityRelationService entityRealationService,
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
  public PageResponse<Manifestation> findChildren(UUID uuid, PageRequest pageRequest) {
    return ((ManifestationRepository) repository).findChildren(uuid, pageRequest);
  }

  @Override
  public Manifestation getByUuid(UUID uuid) throws ServiceException {
    Manifestation manifestation = super.getByUuid(uuid);
    return manifestation;
  }

  @Override
  public Manifestation getByIdentifier(Identifier identifier) {
    // TODO Auto-generated method stub
    return super.getByIdentifier(identifier);
  }

  @Override
  public Manifestation getByRefId(long refId) {
    // TODO Auto-generated method stub
    return super.getByRefId(refId);
  }

  @Override
  public void save(Manifestation manifestation) throws ServiceException, ValidationException {
    super.save(manifestation);
    try {
      List<EntityRelation> entityRelations = manifestation.getRelations();
      Manifestation manifestationWithUuidOnly =
          Manifestation.builder().uuid(manifestation.getUuid()).build();
      entityRelationService.persistEntityRelations(
          manifestation, entityRelations, true, manifestationWithUuidOnly);
      manifestation.setRelations(entityRelations);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot save Manifestation=" + manifestation + ": " + e, e);
    }
  }

  @Override
  public void update(Manifestation manifestation) throws ServiceException, ValidationException {
    super.update(manifestation);
    try {
      List<EntityRelation> entityRelations = manifestation.getRelations();
      Manifestation manifestationWithUuidOnly =
          Manifestation.builder().uuid(manifestation.getUuid()).build();
      entityRelationService.persistEntityRelations(
          manifestation, entityRelations, false, manifestationWithUuidOnly);
      manifestation.setRelations(entityRelations);
    } catch (ServiceException e) {
      throw new ServiceException("Cannot update Manifestation=" + manifestation + ": " + e, e);
    }
  }
}
