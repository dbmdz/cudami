package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CorporationRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ExternalCorporationRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CorporationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorporationServiceImpl extends EntityServiceImpl<Corporation>
    implements CorporationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporationServiceImpl.class);

  private final ExternalCorporationRepository externalRepository;
  private final FileResourceMetadataService fileResourceMetadataService;

  @Autowired
  public CorporationServiceImpl(
      CorporationRepository repository,
      ExternalCorporationRepository externalRepository,
      FileResourceMetadataService fileResourceMetadataService) {
    super(repository);
    this.externalRepository = externalRepository;
    this.fileResourceMetadataService = fileResourceMetadataService;
  }

  @Override
  public Corporation fetchAndSaveByGndId(String gndId) {
    Corporation corporation = externalRepository.getByGndId(gndId);
    try {
      ImageFileResource previewImage =
          (ImageFileResource) fileResourceMetadataService.save(corporation.getPreviewImage());
      corporation.setPreviewImage(previewImage);
    } catch (IdentifiableServiceException ex) {
      LOGGER.warn(
          "Can not save previewImage of corporation: "
              + corporation.getLabel().getText()
              + ", gndId: "
              + gndId);
    }
    return repository.save(corporation);
  }
}
