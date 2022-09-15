package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryImpl extends JdbiRepositoryImpl implements TagRepository {

  // TODO
  public static final String TABLE_NAME = "tags";
  public static final String TABLE_ALIAS = "tags";
  public static final String MAPPING_PREFIX = "tags";

  public static final String SQL_INSERT_FIELDS =
      " uuid, label, namespace, id, tag_type, created, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :label, :namespace, :id, :tagType, :created, :lastModified";
  public static final String SQL_REDUCED_FIELDS_TAGS =
      String.format(
          " %1$s.uuid, %1$s.label,  %1$s.namespace, %1$s.id, %1$s.tag_type, %1$s.created, %1$s.last_modified",
          TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_TAGS = SQL_REDUCED_FIELDS_TAGS;

  public TagRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Tag.class, MAPPING_PREFIX));
  }

  @Override
  public Tag getByUuid(UUID uuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_TAGS
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid = :uuid";

    Tag tag =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapToBean(Tag.class).findOne().orElse(null));

    return tag;
  }

  @Override
  public Tag save(Tag tag) {
    tag.setUuid(UUID.randomUUID());
    tag.setCreated(LocalDateTime.now());
    tag.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ")"
            + " VALUES ("
            + SQL_INSERT_VALUES
            + ")"
            + " RETURNING *";

    Tag result =
        dbi.withHandle(
            h -> h.createQuery(sql).bindBean(tag).mapToBean(Tag.class).findOne().orElse(null));
    return result;
  }

  @Override
  public Tag update(Tag tag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public PageResponse<Tag> find(PageRequest pageRequest) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "namespace":
        return tableAlias + ".namespace";
      case "id":
        return tableAlias + ".id";
      case "tagType":
        return tableAlias + ".tag_type";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    // TODO Auto-generated method stub
    return false;
  }
}
