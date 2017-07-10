package de.digitalcollections.cudami.model.impl.security;

import de.digitalcollections.cudami.model.api.security.Operation;

public class OperationImpl implements Operation<Long> {

  private Long id;
  private String name;

  public OperationImpl() {
  }

  public OperationImpl(String name) {
    this.name = name;
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

}
