package de.digitalcollections.cudami.server.business.impl.service.view;

import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import java.util.Locale;
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
  public RenderingTemplate getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public RenderingTemplate save(RenderingTemplate template) {
    return repository.save(template);
  }

  private void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "name", "uuid");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public RenderingTemplate update(RenderingTemplate template) {
    return repository.update(template);
  }
}
