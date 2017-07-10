package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.model.impl.security.OperationImpl;
import de.digitalcollections.cudami.model.impl.security.RoleImpl;
import de.digitalcollections.cudami.server.backend.api.repository.RoleRepository;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepository<RoleImpl, Long> {

  @Autowired
  private Jdbi dbi;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public RoleImpl create() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(RoleImpl t) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(Iterable<? extends RoleImpl> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean exists(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<RoleImpl> findAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Page<RoleImpl> findAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterable<RoleImpl> findAll() {
    return dbi.withHandle(h -> h.createQuery(
            "SELECT r.name AS name, array_agg(o.name) AS allowedOperations FROM roles r"
            + "  JOIN role_operation ro ON (r.id = ro.role_id)"
            + "  JOIN operations o ON (o.id = ro.operation_id)"
            + "  GROUP BY r.name;")
            .mapToBean(RoleImpl.class)
            .list());
  }

  @Override
  public Iterable<RoleImpl> findAll(Iterable<Long> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public RoleImpl findByName(String name) {
    return dbi.withHandle(h -> h.createQuery(
            "SELECT r.id AS id, r.name AS name, array_agg(o.name) AS allowedOperations FROM roles r"
            + "  JOIN role_operation ro ON (r.id = ro.role_id)"
            + "  JOIN operations o ON (o.id = ro.operation_id)"
            + "  WHERE r.name = :name"
            + "  GROUP BY r.name, r.id;")
            .bind("name", name)
            .map(new RoleImplMapper())
            .findOnly());
  }

  @Override
  public RoleImpl findOne(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <S extends RoleImpl> S save(S s) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <S extends RoleImpl> Iterable<S> save(Iterable<S> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * custom mapper for role to correctly map the list of allowed operations
   */
  private class RoleImplMapper implements RowMapper<RoleImpl> {

    @Override
    public RoleImpl map(ResultSet rs, StatementContext ctx) throws SQLException {
      Long id = rs.getLong("id");
      String name = rs.getString("name");
      Array allowedOperations = rs.getArray("allowedOperations");

      RoleImpl result = new RoleImpl(name);
      result.setId(id);
      ColumnMapper<String[]> arrayMapper = ctx.findColumnMapperFor(String[].class).get();
      String[] opNames = arrayMapper.map(rs, "allowedOperations", ctx);
      result.setAllowedOperations(Arrays.stream(opNames)
              .map(OperationImpl::new)
              .collect(Collectors.toList()));
      return result;
    }
  }
}
