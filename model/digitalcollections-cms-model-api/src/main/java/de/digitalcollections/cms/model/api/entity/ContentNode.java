package de.digitalcollections.cms.model.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Category is used to structure content hierarchically.
 *
 * @param <ID> unique id specifying instance
 */
public interface ContentNode<ID extends Serializable> extends Entity<ID> {

  ContentNode getParent();

  void setParent(ContentNode parent);

  List<ContentNode> getChildren();

  void setChildren(List<ContentNode> children);

  default void addChild(ContentNode child) {
    if (getChildren() == null) {
      setChildren(new ArrayList<>());
    }
    getChildren().add(child);
  }
  
  List<TextContent> getTextContents();

  void setTextContents(List<TextContent> textContents);

  default void addTextContent(TextContent textContent) {
    if (getTextContents() == null) {
      setTextContents(new ArrayList<>());
    }
    getTextContents().add(textContent);
  }
  
  List<BinaryContent> getBinaryContents();

  void setBinaryContents(List<BinaryContent> binaryContents);

  default void addBinaryContent(BinaryContent binaryContent) {
    if (getBinaryContents() == null) {
      setBinaryContents(new ArrayList<>());
    }
    getBinaryContents().add(binaryContent);
  }

  List<IiifContent> getIiifContents();

  void setIiifContents(List<IiifContent> iiifContents);

  default void addIiifContent(IiifContent iiifContent) {
    if (getIiifContents() == null) {
      setIiifContents(new ArrayList<>());
    }
    getIiifContents().add(iiifContent);
  }
}
