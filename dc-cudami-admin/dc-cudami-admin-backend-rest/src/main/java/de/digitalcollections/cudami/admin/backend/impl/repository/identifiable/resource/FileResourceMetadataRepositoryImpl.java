package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl.FindParams;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceMetadataRepositoryImpl extends IdentifiableRepositoryImpl<FileResource>
    implements FileResourceMetadataRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataRepositoryImpl.class);

  @Autowired private FileResourceMetadataRepositoryEndpoint endpoint;

  @Autowired ObjectMapper objectMapper;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public FileResource create() {
    return new FileResourceImpl();
  }

  @Override
  public PageResponse<FileResource> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<FileResource> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public FileResource findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public FileResource findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public FileResource save(FileResource fileResource) {
    return endpoint.save(fileResource);
  }

  @Override
  public FileResource update(FileResource fileResource) {
    return endpoint.update(fileResource.getUuid(), fileResource);
  }
}
