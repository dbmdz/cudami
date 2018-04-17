package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.NodeImpl;
import java.util.List;
import de.digitalcollections.prosemirror.model.api.Document;

public class WebpageImpl extends ResourceImpl implements Webpage, Node<Webpage> {

  private Text contentBlocks;

  private final NodeImpl<Webpage> node;
  private Document contentBlocksContainer;

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
  public Text getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(Text contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

  @Override
  public Document getContentBlocksContainer() {
    return contentBlocksContainer;
  }

  @Override
  public void setContentBlocksContainer(Document contentBlocksContainer) {
    this.contentBlocksContainer = contentBlocksContainer;
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
