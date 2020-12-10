package de.digitalcollections.cudami.server.business.impl.service.view;

import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.view.RenderingTemplate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RenderingTemplateServiceImpl implements RenderingTemplateService {
  private final RenderingTemplateRepository repository;

  public RenderingTemplateServiceImpl(RenderingTemplateRepository repository) {
    this.repository = repository;
  }

  @Override
  public PageResponse<RenderingTemplate> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public RenderingTemplate findOne(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public RenderingTemplate save(RenderingTemplate template) {
    return repository.save(template);
  }

  @Override
  public RenderingTemplate update(RenderingTemplate template) {
    return repository.update(template);
  }
}
