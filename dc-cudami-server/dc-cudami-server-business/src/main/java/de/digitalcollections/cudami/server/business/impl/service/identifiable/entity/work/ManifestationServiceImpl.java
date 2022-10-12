package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import java.util.List;
import java.util.stream.Collectors;
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
  public Manifestation save(Manifestation manifestation)
      throws IdentifiableServiceException, ValidationException {
    Manifestation savedManifestation = super.save(manifestation);
    savedManifestation = persistEntityRelations(savedManifestation, true);
    return savedManifestation;
  }

  @Override
  public Manifestation update(Manifestation manifestation)
      throws IdentifiableServiceException, ValidationException {
    Manifestation updatedManifestation = super.update(manifestation);
    updatedManifestation = persistEntityRelations(updatedManifestation, false);
    return updatedManifestation;
  }

  private Manifestation persistEntityRelations(
      Manifestation manifestation, boolean deleteExisting) {
    if (deleteExisting) {
      // Check, if there are already persisted EntityRelations for the manifestation
      // If yes, delete them
      entityRelationService.deleteByObject(manifestation);
    }

    // save all entity relations and set the UUID of the object
    List<EntityRelation> relations =
        manifestation.getRelations().stream()
            .map(
                r -> {
                  r.setObject(manifestation);
                  return r;
                })
            .collect(Collectors.toList());
    relations = entityRelationService.save(relations);
    manifestation.setRelations(relations);

    return manifestation;
  }
}
