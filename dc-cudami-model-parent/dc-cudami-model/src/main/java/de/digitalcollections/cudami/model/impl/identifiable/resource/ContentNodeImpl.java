package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.impl.identifiable.NodeImpl;
import java.util.List;

public class ContentNodeImpl extends ResourceImpl implements ContentNode, Node<ContentNode> {

  private final NodeImpl<ContentNode> node;
  private List<ContentNode> subNodes;

  public ContentNodeImpl() {
    super();
    this.node = new NodeImpl<>();
    this.resourceType = ResourceType.CONTENT_NODE;
  }

  @Override
  public void addChild(ContentNode child) {
    node.addChild(child);
  }

  @Override
  public void addContent(Identifiable identifiable) {
    node.addContent(identifiable);
  }

  @Override
  public List<ContentNode> getChildren() {
    return node.getChildren();
  }

  @Override
  public void setChildren(List<ContentNode> children) {
    node.setChildren(children);
  }

  @Override
  public List<Identifiable> getContent() {
    return node.getContent();
  }

  @Override
  public void setContent(List<Identifiable> content) {
    node.setContent(content);
  }

  @Override
  public ContentNode getParent() {
    return node.getParent();
  }

  @Override
  public void setParent(ContentNode parent) {
    node.setParent(parent);
  }

  @Override
  public ResourceType getResourceType() {
    return resourceType;
  }

  @Override
  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  @Override
  public List<ContentNode> getSubNodes() {
    return subNodes;
  }

  @Override
  public void setSubNodes(List<ContentNode> subNodes) {
    this.subNodes = subNodes;
  }

}
