package de.digitalcollections.cudami.server.business.impl.service.view;

import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.paging.SortingImpl;
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
    setDefaultSorting(pageRequest);
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

  private void setDefaultSorting(PageRequest pageRequest) {
    if (pageRequest.getSorting() == null
        || pageRequest.getSorting().getOrders() == null
        || pageRequest.getSorting().getOrders().isEmpty()) {
      Sorting sorting = new SortingImpl(Direction.ASC, "name");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public RenderingTemplate update(RenderingTemplate template) {
    return repository.update(template);
  }
}
