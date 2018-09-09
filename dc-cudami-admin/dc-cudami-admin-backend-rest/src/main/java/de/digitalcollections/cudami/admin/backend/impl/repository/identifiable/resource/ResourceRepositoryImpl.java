package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.*;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.identifiable.resource.ResourceImpl;
import feign.form.FormData;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepositoryImpl<R extends Resource> extends IdentifiableRepositoryImpl<R> implements ResourceRepository<R> {

  @Autowired
  private LocaleRepository localeRepository;

  @Autowired
  private ResourceRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public R create() {
    Locale defaultLocale = localeRepository.getDefault();
    R resource = (R) new ResourceImpl();
    resource.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    resource.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return resource;
  }

  @Override
  public PageResponse<R> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Resource> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public R findOne(UUID uuid) {
    return (R) endpoint.findOne(uuid);
  }

  @Override
  public R save(R identifiable) {
    return (R) endpoint.save(identifiable);
  }

  @Override
  public R save(R resource, FileResource fileResource, byte[] bytes) {
    String contentType = fileResource.getMimeType().getTypeName();
    FormData formData = new FormData(contentType, bytes);
    return (R) endpoint.save(resource, formData);
  }

  @Override
  public R update(R identifiable) {
    return (R) endpoint.update(identifiable.getUuid(), identifiable);
  }
}
