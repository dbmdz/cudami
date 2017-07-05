package de.digitalcollections.cms.model.impl.security;

import de.digitalcollections.cms.model.api.security.Operation;
import de.digitalcollections.cms.model.api.security.Role;
import java.util.ArrayList;
import java.util.List;

public class RoleImpl implements Role<Long> {

  private Long id;
  private String name;
  private List<Operation> operations = new ArrayList<>(0);

  public RoleImpl() {
  }

  public RoleImpl(String name) {

  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public List<Operation> getAllowedOperations() {
    return operations;
  }

  @Override
  public void setAllowedOperations(List<Operation> allowedOperations) {
    this.operations = allowedOperations;
  }

  @Override
  public String getAuthority() {
    return getName();
  }

}
