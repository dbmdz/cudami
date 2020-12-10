package de.digitalcollections.cudami.server.business.api.service.view;

import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.view.RenderingTemplate;
import java.util.UUID;

public interface RenderingTemplateService {

  PageResponse<RenderingTemplate> find(PageRequest pageRequest);

  RenderingTemplate findOne(UUID uuid);

  RenderingTemplate save(RenderingTemplate template);

  RenderingTemplate update(RenderingTemplate template);
}
