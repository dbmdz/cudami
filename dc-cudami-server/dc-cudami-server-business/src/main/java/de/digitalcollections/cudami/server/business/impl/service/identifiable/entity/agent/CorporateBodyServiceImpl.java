package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.ExternalCorporateBodyRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorporateBodyServiceImpl extends EntityServiceImpl<CorporateBody>
    implements CorporateBodyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodyServiceImpl.class);

  private final ExternalCorporateBodyRepository externalRepository;
  private final FileResourceMetadataService fileResourceMetadataService;

  @Autowired
  public CorporateBodyServiceImpl(
      CorporateBodyRepository repository,
      ExternalCorporateBodyRepository externalRepository,
      FileResourceMetadataService fileResourceMetadataService) {
    super(repository);
    this.externalRepository = externalRepository;
    this.fileResourceMetadataService = fileResourceMetadataService;
  }

  @Override
  public CorporateBody fetchAndSaveByGndId(String gndId) {
    CorporateBody corporateBody = externalRepository.getByGndId(gndId);
    if (corporateBody == null) {
      return null;
    }

    if (corporateBody.getPreviewImage() != null) {
      try {
        ImageFileResource previewImage =
            (ImageFileResource) fileResourceMetadataService.save(corporateBody.getPreviewImage());
        corporateBody.setPreviewImage(previewImage);
      } catch (IdentifiableServiceException ex) {
        LOGGER.warn(
            "Can not save previewImage of corporate body: "
                + corporateBody.getLabel().getText()
                + ", gndId: "
                + gndId);
      }
    }
    return repository.save(corporateBody);
  }
}
