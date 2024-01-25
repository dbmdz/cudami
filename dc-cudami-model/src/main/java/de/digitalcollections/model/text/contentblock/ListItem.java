package de.digitalcollections.model.text.contentblock;

/** A list item of an (un)ordered list. */
public class ListItem extends ContentBlockNode {

  public ListItem() {}

  public ListItem(String text) {
    addContentBlock(new Paragraph(text));
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{hashCode=" + hashCode() + "}";
  }
}
