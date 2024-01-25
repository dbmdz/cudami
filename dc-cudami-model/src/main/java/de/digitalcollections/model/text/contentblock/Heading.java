package de.digitalcollections.model.text.contentblock;

/** A heading of specific level. */
public class Heading extends ContentBlockNodeWithAttributes {

  public Heading() {}

  public Heading(int level, String text) {
    addContentBlock(new Text(text));
    addAttribute("level", level);
  }
}
