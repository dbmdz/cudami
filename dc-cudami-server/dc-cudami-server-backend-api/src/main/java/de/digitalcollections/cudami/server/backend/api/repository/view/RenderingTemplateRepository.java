package de.digitalcollections.cudami.server.backend.api.repository.view;

import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.UUID;

/** Repository for rendering templates persistence handling. */
public interface RenderingTemplateRepository {

  /**
   * Return count of templates.
   *
   * @return the count of templates
   */
  long count();

  /**
   * Return all templates
   *
   * @param pageRequest the paging parameters
   * @return Paged list of all rendering templates
   */
  PageResponse<RenderingTemplate> find(PageRequest pageRequest);

  /**
   * Return template with uuid
   *
   * @param uuid the uuid of the rendering template
   * @return The found rendering template
   */
  RenderingTemplate getByUuid(UUID uuid);

  /**
   * Save a template.
   *
   * @param template the template to be saved
   * @return the saved template
   */
  RenderingTemplate save(RenderingTemplate template);

  /**
   * Update a template.
   *
   * @param template the template to be updated
   * @return the updated template
   */
  RenderingTemplate update(RenderingTemplate template);
}
