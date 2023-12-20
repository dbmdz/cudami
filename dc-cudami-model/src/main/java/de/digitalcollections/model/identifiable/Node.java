package de.digitalcollections.model.identifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * Node is used to structure cultural content hierarchically.
 *
 * @param <N> a node
 */
public class Node<N extends Identifiable> extends Identifiable implements INode<N> {

  private List<N> children;

  private N parent;

  public Node() {
    super();
    children = new ArrayList<>();
  }

  @Override
  public List<N> getChildren() {
    return children;
  }

  @Override
  public N getParent() {
    return parent;
  }

  @Override
  public void setChildren(List<N> children) {
    this.children = children;
  }

  @Override
  public void setParent(N parent) {
    this.parent = parent;
  }
}
