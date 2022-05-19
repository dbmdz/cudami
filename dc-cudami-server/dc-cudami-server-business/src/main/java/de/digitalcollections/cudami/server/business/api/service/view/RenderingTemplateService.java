package de.digitalcollections.cudami.server.business.api.service.view;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.UUID;

public interface RenderingTemplateService {

  PageResponse<RenderingTemplate> find(PageRequest pageRequest);

  RenderingTemplate getByUuid(UUID uuid);

  RenderingTemplate save(RenderingTemplate template);

  RenderingTemplate update(RenderingTemplate template);
}
