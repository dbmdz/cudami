package de.digitalcollections.model.identifiable.web;

import de.digitalcollections.model.content.ManagedContent;
import de.digitalcollections.model.content.PublicationStatus;
import de.digitalcollections.model.identifiable.INode;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingHints;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/** A Webpage of a Website. */
@SuperBuilder(buildMethodName = "prebuild")
public class Webpage extends Identifiable implements INode<Webpage>, ManagedContent {

  private Node<Webpage> node;
  private LocalDate publicationEnd;
  private LocalDate publicationStart;
  private PublicationStatus publicationStatus;
  private RenderingHints renderingHints;
  private LocalizedStructuredContent text;

  public Webpage() {
    super();
  }

  public Webpage(List<Webpage> children) {
    this();
    this.node.setChildren(children);
  }

  @Override
  public List<Webpage> getChildren() {
    return node.getChildren();
  }

  @Override
  public LocalizedText getLabel() {
    return label;
  }

  @Override
  public Webpage getParent() {
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

  public RenderingHints getRenderingHints() {
    return renderingHints;
  }

  public LocalizedStructuredContent getText() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Webpage)) return false;
    if (!super.equals(o)) return false;
    Webpage webpage = (Webpage) o;
    return Objects.equals(node, webpage.node)
        && Objects.equals(publicationEnd, webpage.publicationEnd)
        && Objects.equals(publicationStart, webpage.publicationStart)
        && publicationStatus == webpage.publicationStatus
        && Objects.equals(renderingHints, webpage.renderingHints)
        && Objects.equals(text, webpage.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        node,
        publicationEnd,
        publicationStart,
        publicationStatus,
        renderingHints,
        text);
  }

  @Override
  public String toString() {
    return "Webpage{"
        + "node="
        + node
        + ", publicationEnd="
        + publicationEnd
        + ", publicationStart="
        + publicationStart
        + ", publicationStatus="
        + publicationStatus
        + ", renderingHints="
        + renderingHints
        + ", text="
        + text
        + ", "
        + super.toString()
        + '}';
  }

  @Override
  protected void init() {
    super.init();
    this.type = IdentifiableType.RESOURCE;
    if (node == null) {
      node = new Node<>();
    }
    if (renderingHints == null) {
      renderingHints = new RenderingHints();
    }
  }

  @Override
  public void setChildren(List<Webpage> children) {
    node.setChildren(children);
  }

  @Override
  public void setParent(Webpage parent) {
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

  public void setRenderingHints(RenderingHints renderingHints) {
    this.renderingHints = renderingHints;
  }

  public void setText(LocalizedStructuredContent text) {
    this.text = text;
  }

  public abstract static class WebpageBuilder<C extends Webpage, B extends WebpageBuilder<C, B>>
      extends IdentifiableBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B children(List<Webpage> children) {
      if (node == null) {
        node = new Node<>();
      }
      node.setChildren(children);
      return self();
    }

    public B notShownInNavigation() {
      if (renderingHints == null) {
        renderingHints = new RenderingHints();
      }
      renderingHints.setShowInPageNavigation(false);
      return self();
    }

    public B publicationEnd(LocalDate publicationEnd) {
      this.publicationEnd = publicationEnd;
      return self();
    }

    public B publicationEnd(String publicationEnd) {
      this.publicationEnd = LocalDate.parse(publicationEnd);
      return self();
    }

    public B publicationStart(LocalDate publicationStart) {
      this.publicationStart = publicationStart;
      return self();
    }

    public B publicationStart(String publicationStart) {
      this.publicationStart = LocalDate.parse(publicationStart);
      return self();
    }

    public B shownInNavigation() {
      if (renderingHints == null) {
        renderingHints = new RenderingHints();
      }
      renderingHints.setShowInPageNavigation(true);
      return self();
    }

    public B templateName(String templateName) {
      if (renderingHints == null) {
        renderingHints = new RenderingHints();
      }
      renderingHints.setTemplateName(templateName);
      return self();
    }
  }
}
