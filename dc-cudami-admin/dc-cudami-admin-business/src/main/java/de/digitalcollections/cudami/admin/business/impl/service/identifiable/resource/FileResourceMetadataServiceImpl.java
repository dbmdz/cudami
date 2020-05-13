package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.admin.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
// @Transactional(readOnly = true)
public class FileResourceMetadataServiceImpl extends IdentifiableServiceImpl<FileResource>
    implements FileResourceMetadataService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataServiceImpl.class);

  @Autowired
  public FileResourceMetadataServiceImpl(FileResourceMetadataRepository repository) {
    super(repository);
  }

  @Override
  public SearchPageResponse<FileResource> findImages(SearchPageRequest searchPageRequest) {
    return ((FileResourceMetadataRepository) repository).findImages(searchPageRequest);
  }
}
