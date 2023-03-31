package de.digitalcollections.cudami.server.business.impl.service.view;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RenderingTemplateServiceImpl
    extends UniqueObjectServiceImpl<RenderingTemplate, RenderingTemplateRepository>
    implements RenderingTemplateService {

  public RenderingTemplateServiceImpl(RenderingTemplateRepository repository) {
    super(repository);
  }

  @Override
  public List<Locale> getLanguages() throws ServiceException {
    try {
      return repository.getLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "name", "uuid");
      pageRequest.setSorting(sorting);
    }
  }
}
