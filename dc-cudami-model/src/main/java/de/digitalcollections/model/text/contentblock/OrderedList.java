package de.digitalcollections.model.text.contentblock;

/** An ordered list. */
public class OrderedList extends ContentBlockNodeWithAttributes {

  public OrderedList() {}

  public OrderedList(int order) {
    addAttribute("order", 1);
  }
}
