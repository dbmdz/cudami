package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.identifiable.INode;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;

/**
 * https://www.merriam-webster.com/dictionary/topic: "the subject of something that is being
 * discussed or has been written or thought about"
 * https://www.linguee.com/english-german/translation/topic.html german: "Thema, Inhalt, Gegenstand,
 * Gesprächsthema" or "Thematik, Themengebiet, Themenbereich"
 *
 * <p>Subtopic: https://www.merriam-webster.com/dictionary/subtopic: "a (subtopic is a) topic that
 * is part of a broader or more general topic ..." "a (subtopic is a) topic that is one of the parts
 * or divisions of the main topic ..." "a (subtopic is a) topic (as in a composition) that is a
 * division of a main topic" https://www.linguee.com/english-german/translation/subtopic.html
 * german: "Unterthema"
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Topic extends Entity implements INode<Topic> {

  private List<Entity> entities;
  private List<FileResource> fileResources;
  private Node<Topic> node;

  public Topic() {
    super();
  }

  public Topic(List<Topic> children) {
    this();
    node.setChildren(children);
  }

  @Override
  public void addChild(Topic child) {
    node.addChild(child);
  }

  public void addEntity(Entity entity) {
    if (getEntities() == null) {
      setEntities(new ArrayList<>(0));
    }
    getEntities().add(entity);
  }

  public void addFileResource(FileResource fileResource) {
    if (getFileResources() == null) {
      setFileResources(new ArrayList<>(0));
    }
    getFileResources().add(fileResource);
  }

  @Override
  public List<Topic> getChildren() {
    return node.getChildren();
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public List<FileResource> getFileResources() {
    return fileResources;
  }

  @Override
  public Topic getParent() {
    return node.getParent();
  }

  @Override
  protected void init() {
    super.init();
    if (node == null) node = new Node<>();
    if (entities == null) entities = new ArrayList<>(0);
    if (fileResources == null) fileResources = new ArrayList<>(0);
  }

  @Override
  public void setChildren(List<Topic> children) {
    node.setChildren(children);
  }

  public void setEntities(List<Entity> entities) {
    this.entities = entities;
  }

  public void setFileResources(List<FileResource> fileResources) {
    this.fileResources = fileResources;
  }

  @Override
  public void setParent(Topic parent) {
    node.setParent(parent);
  }

  public abstract static class TopicBuilder<C extends Topic, B extends TopicBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }
  }
}
