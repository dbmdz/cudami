package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.resource.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.NodeImpl;
import java.util.List;

public class WebpageImpl extends ResourceImpl implements Webpage, Node<Webpage> {

  private final NodeImpl<Webpage> node;
  private MultilanguageDocument multilanguageDocument;

  public WebpageImpl() {
    super();
    this.node = new NodeImpl<>();
    this.resourceType = ResourceType.WEBPAGE;
  }

  @Override
  public void addChild(Webpage child) {
    node.addChild(child);
  }

  @Override
  public void addContent(Identifiable identifiable) {
    node.addContent(identifiable);
  }

  @Override
  public List<Webpage> getChildren() {
    return node.getChildren();
  }

  @Override
  public void setChildren(List<Webpage> children) {
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
  public MultilanguageDocument getMultilanguageDocument() {
    return multilanguageDocument;
  }

  @Override
  public void setMultilanguageDocument(MultilanguageDocument multilanguageDocument) {
    this.multilanguageDocument = multilanguageDocument;
  }

  @Override
  public Webpage getParent() {
    return node.getParent();
  }

  @Override
  public void setParent(Webpage parent) {
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
}
