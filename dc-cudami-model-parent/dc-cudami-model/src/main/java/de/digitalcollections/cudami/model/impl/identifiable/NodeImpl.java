package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import java.util.List;

public class NodeImpl extends IdentifiableImpl implements Node {

  private Node parent;
  private List<Node> children;
  private Text label;
  private List<Identifiable> content;

  @Override
  public Node getParent() {
    return parent;
  }

  @Override
  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public List<Node> getChildren() {
    return children;
  }

  @Override
  public void setChildren(List<Node> children) {
    this.children = children;
  }

  @Override
  public Text getLabel() {
    return label;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
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
