package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import java.util.List;

public class NodeImpl<N extends Node> extends IdentifiableImpl implements Node<N> {

  private N parent;
  private List<N> children;
  private List<Identifiable> content;

  @Override
  public N getParent() {
    return parent;
  }

  @Override
  public void setParent(N parent) {
    this.parent = parent;
  }

  @Override
  public List<N> getChildren() {
    return children;
  }

  @Override
  public void setChildren(List<N> children) {
    this.children = children;
  }

  @Override
  public List<Identifiable> getContent() {
    return content;
  }

  @Override
  public void setContent(List<Identifiable> content) {
    this.content = content;
  }

}
