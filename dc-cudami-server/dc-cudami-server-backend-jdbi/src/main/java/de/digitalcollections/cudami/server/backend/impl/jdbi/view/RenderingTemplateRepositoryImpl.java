package de.digitalcollections.cudami.server.backend.impl.jdbi.view;

import de.digitalcollections.cudami.server.backend.api.repository.view.RenderingTemplateRepository;
import de.digitalcollections.cudami.server.backend.impl.database.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class RenderingTemplateRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements RenderingTemplateRepository {

  public static final String MAPPING_PREFIX = "r";
  public static final String TABLE_ALIAS = "rt";
  public static final String TABLE_NAME = "rendering_templates";

  private final Jdbi dbi;

  public RenderingTemplateRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM rendering_templates";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<RenderingTemplate> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM rendering_templates");
    addPageRequestParams(pageRequest, query);
    List<RenderingTemplate> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(RenderingTemplate.class).list());
    long total = count();
    return new PageResponse(result, pageRequest, total);
  }

  @Override
  public RenderingTemplate findOne(UUID uuid) {
    String query = "SELECT * FROM rendering_templates WHERE uuid=:uuid";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bind("uuid", uuid)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("label", "name"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "label":
        return "label";
      case "name":
        return "name";
      case "uuid":
        return "uuid";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  public RenderingTemplate save(RenderingTemplate template) {
    template.setUuid(UUID.randomUUID());
    String query =
        "INSERT INTO rendering_templates(description, label, name, uuid)"
            + " VALUES (:description::JSONB, :label::JSONB, :name, :uuid)"
            + " RETURNING *";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bindBean(template)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }

  @Override
  public RenderingTemplate update(RenderingTemplate template) {
    String query =
        "UPDATE rendering_templates"
            + " SET description=:description::JSONB, label=:label::JSONB, name=:name"
            + " WHERE uuid=:uuid RETURNING *";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bindBean(template)
                .mapToBean(RenderingTemplate.class)
                .findOne()
                .orElse(null));
  }
}
