package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;

public interface FileResourceMetadataService extends IdentifiableService<FileResource> {
  SearchPageResponse<FileResource> findImages(SearchPageRequest searchPageRequest);
}
