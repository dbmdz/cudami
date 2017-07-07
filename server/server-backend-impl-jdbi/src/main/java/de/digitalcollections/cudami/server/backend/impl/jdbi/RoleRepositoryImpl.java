package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.model.impl.security.RoleImpl;
import de.digitalcollections.cudami.server.backend.api.repository.RoleRepository;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
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
            "SELECT r.name AS name, array_agg(o.name) AS allowedOperations FROM roles r"
            + "  JOIN role_operation ro ON (r.id = ro.role_id)"
            + "  JOIN operations o ON (o.id = ro.operation_id)"
            + "  WHERE r.name = :name"
            + "  GROUP BY r.name;")
            .bind("name", name)
            .mapToBean(RoleImpl.class)
            // TODO create mapper (problem was setter wants list of operations instead of list of strings...)
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

}
