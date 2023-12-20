package de.digitalcollections.model.identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Node)) return false;
    if (!super.equals(o)) return false;
    Node<?> node = (Node<?>) o;
    return Objects.equals(children, node.children) && Objects.equals(parent, node.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), children, parent);
  }
}
