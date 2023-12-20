package de.digitalcollections.model.text.contentblock;

/** A hard break in text. */
public class HardBreak extends ContentBlock {

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof HardBreak);
  }

  @Override
  public int hashCode() {
    return -1;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{hashCode=" + hashCode() + "}";
  }
}
