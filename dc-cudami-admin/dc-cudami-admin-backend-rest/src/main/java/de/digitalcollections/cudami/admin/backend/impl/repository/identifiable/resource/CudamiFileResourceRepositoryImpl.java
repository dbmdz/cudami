package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl.FindParams;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import feign.form.FormData;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CudamiFileResourceRepositoryImpl<F extends FileResource> extends IdentifiableRepositoryImpl<F> implements CudamiFileResourceRepository<F> {

  @Autowired
  private CudamiFileResourceRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public F create() {
    return (F) new FileResourceImpl();
  }

  @Override
  public PageResponse<F> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<FileResource> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public F findOne(UUID uuid) {
    return (F) endpoint.findOne(uuid);
  }

  @Override
  public F save(F identifiable) {
    return (F) endpoint.save(identifiable);
  }

  @Override
  public F save(FileResource fileResource, byte[] bytes) {
    String contentType = fileResource.getMimeType().getTypeName();
    FormData formData = new FormData(contentType, bytes);
    return (F) endpoint.save(fileResource, formData);
  }

  @Override
  public F update(F identifiable) {
    return (F) endpoint.update(identifiable.getUuid(), identifiable);
  }
}
