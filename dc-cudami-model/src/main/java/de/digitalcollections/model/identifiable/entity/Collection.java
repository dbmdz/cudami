package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.content.ManagedContent;
import de.digitalcollections.model.content.PublicationStatus;
import de.digitalcollections.model.identifiable.INode;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/** A collection of {@link Entity}s. */
@SuperBuilder(buildMethodName = "prebuild")
public class Collection extends Entity implements INode<Collection>, ManagedContent {

  private List<Entity> entities;
  private Node<Collection> node;
  private LocalDate publicationEnd;
  private LocalDate publicationStart;
  private PublicationStatus publicationStatus;

  private LocalizedStructuredContent text;

  public Collection() {
    super();
  }

  public void addEntity(Entity entity) {
    if (getEntities() == null) {
      setEntities(new ArrayList<>(0));
    }
    getEntities().add(entity);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Collection)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Collection that = (Collection) o;
    return Objects.equals(entities, that.entities)
        && Objects.equals(node, that.node)
        && Objects.equals(publicationEnd, that.publicationEnd)
        && Objects.equals(publicationStart, that.publicationStart)
        && Objects.equals(publicationStatus, that.publicationStatus)
        && Objects.equals(text, that.text);
  }

  @Override
  public List<Collection> getChildren() {
    return node.getChildren();
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public Node<Collection> getNode() {
    return node;
  }

  @Override
  public Collection getParent() {
    return node.getParent();
  }

  @Override
  public LocalDate getPublicationEnd() {
    return publicationEnd;
  }

  @Override
  public LocalDate getPublicationStart() {
    return publicationStart;
  }

  @Override
  public PublicationStatus getPublicationStatus() {
    return publicationStatus;
  }

  public LocalizedStructuredContent getText() {
    return text;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), entities, node, publicationEnd, publicationStart, text);
  }

  @Override
  protected void init() {
    super.init();
    if (entities == null) entities = new ArrayList<>();
    if (node == null) node = new Node<>();
  }

  @Override
  public void setChildren(List<Collection> children) {
    node.setChildren(children);
  }

  public void setEntities(List<Entity> entities) {
    this.entities = entities;
  }

  @Override
  public void setParent(Collection parent) {
    node.setParent(parent);
  }

  @Override
  public void setPublicationEnd(LocalDate publicationEnd) {
    this.publicationEnd = publicationEnd;
  }

  @Override
  public void setPublicationStart(LocalDate publicationStart) {
    this.publicationStart = publicationStart;
  }

  @Override
  public void setPublicationStatus(PublicationStatus publicationStatus) {
    this.publicationStatus = publicationStatus;
  }

  public void setText(LocalizedStructuredContent text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{entities="
        + entities
        + ", node="
        + node
        + ", publicationEnd="
        + publicationEnd
        + ", publicationStart="
        + publicationStart
        + ", publicationState="
        + publicationStatus
        + ", text="
        + text
        + ", customAttributes="
        + customAttributes
        + ", refId="
        + refId
        + ", created="
        + created
        + ", description="
        + description
        + ", label="
        + label
        + ", lastModified="
        + lastModified
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", type="
        + type
        + ", uuid="
        + getUuid()
        + "}";
  }

  public abstract static class CollectionBuilder<
          C extends Collection, B extends CollectionBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B publicationEnd(String publicationEnd) {
      this.publicationEnd = LocalDate.parse(publicationEnd);
      return self();
    }

    public B publicationStart(String publicationStart) {
      this.publicationStart = LocalDate.parse(publicationStart);
      return self();
    }
  }
}
