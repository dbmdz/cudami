package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.semantic.Subject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectRepositoryImpl extends JdbiRepositoryImpl implements SubjectRepository {

  public static final String TABLE_NAME = "subjects";
  public static final String TABLE_ALIAS = "subj";
  public static final String MAPPING_PREFIX = "subj";

  public static final String SQL_INSERT_FIELDS =
      " uuid, label, type, identifiers, created, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :label::JSONB, :type, :identifiers, :created, :lastModified";
  public static final String SQL_REDUCED_FIELDS_SUBJECTS =
      String.format(
          " %1$s.uuid as %2$s_uuid, %1$s.label as %2$s_label, %1$s.identifiers as %2$s_identifiers, %1$s.type as %2$s_type, %1$s.created as %2$s_created, %1$s.last_modified as %2$s_last_modified",
          TABLE_ALIAS, MAPPING_PREFIX);
  public static final String SQL_FULL_FIELDS_SUBJECTS = SQL_REDUCED_FIELDS_SUBJECTS;

  public SubjectRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Subject.class, MAPPING_PREFIX));
    this.dbi.registerArrayType(new DbIdentifierMapper());
    this.dbi.registerColumnMapper(Identifier.class, new DbIdentifierMapper());
  }

  @Override
  public Subject getByUuid(UUID uuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_SUBJECTS
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid = :uuid";

    Subject subject =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Subject.class).findOne().orElse(null));

    return subject;
  }

  @Override
  public Subject save(Subject subject) {
    subject.setUuid(UUID.randomUUID());
    subject.setCreated(LocalDateTime.now());
    subject.setLastModified(LocalDateTime.now());

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

    Subject result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(subject)
                    .mapToBean(Subject.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Subject update(Subject subject) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PageResponse<Subject> find(PageRequest pageRequest) {
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getUniqueField() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    // TODO Auto-generated method stub
    return false;
  }

  private class DbIdentifierArgumentFactory extends AbstractArgumentFactory<Identifier> {

    protected DbIdentifierArgumentFactory() {
      super(Types.JAVA_OBJECT);
    }

    @Override
    protected Argument build(Identifier value, ConfigRegistry config) {
      return (position, statement, ctx) ->
          statement.setString(
              position, "ROW('" + value.getNamespace() + "','" + value.getId() + "')");
    }
  }
}
